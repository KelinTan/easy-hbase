// Copyright 2019 Kelin Inc. All rights reserved.

package com.kelin.easy.hbase.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * @author Kelin Tan
 */
public class JsonMapperFactory {
    private static ObjectMapper defaultMapper = new ObjectMapper();

    static {
        defaultMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS)
                .disable(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES)
                .setNodeFactory(JsonNodeFactory.withExactBigDecimals(true))
        ;
    }

    static ObjectMapper defaultMapper() {
        return defaultMapper;
    }
}
