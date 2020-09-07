// Copyright 2019 Kelin Inc. All rights reserved.

package com.kelin.easy.hbase.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kelin Tan
 */
public class JsonConverter {
    private static Logger log = LoggerFactory.getLogger(JsonConverter.class);

    private static final ObjectMapper DEFAULT_MAPPER = JsonMapperFactory.defaultMapper();

    public static Object deserialize(String json, Type type) {
        try {
            return DEFAULT_MAPPER.readValue(json, DEFAULT_MAPPER.getTypeFactory().constructType(type));
        } catch (IOException e) {
            log.error("json deserialize error", e);
            return null;
        }
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return DEFAULT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json deserialize error", e);
            return null;
        }
    }

    public static <T> List<T> deserializeList(String json, Class<T> clazz) {
        try {
            return DEFAULT_MAPPER.readValue(json, DEFAULT_MAPPER
                    .getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json deserialize error", e);
            return null;
        }
    }

    public static <T, E> Map<T, E> deserializeMap(String json, Class<T> key, Class<E> value) {
        try {
            return DEFAULT_MAPPER.readValue(json, DEFAULT_MAPPER
                    .getTypeFactory().constructMapType(HashMap.class, key, value));
        } catch (IOException e) {
            log.error("json deserialize error", e);
            return null;
        }
    }

    public static <T> T deserializeGenerics(String json, TypeReference<T> typeReference) {
        try {
            return DEFAULT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("json deserialize error", e);
            return null;
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return DEFAULT_MAPPER.readTree(json);
        } catch (IOException e) {
            log.error("json read tree error", e);
            return null;
        }
    }

    public static String serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("json serialize error", e);
            return null;
        }
    }

    public static byte[] serializeAsBytes(Object obj) {
        try {
            return DEFAULT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("json serialize error", e);
            return null;
        }
    }
}
