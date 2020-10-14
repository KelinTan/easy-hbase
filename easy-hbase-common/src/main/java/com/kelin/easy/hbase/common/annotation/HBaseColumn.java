// Copyright 2020 Kelin Inc. All rights reserved.

package com.kelin.easy.hbase.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Kelin Tan
 *         <p>
 *         used on the field associate to the column from the hbase
 *         </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HBaseColumn {
    String family() default "";

    String column();

    boolean exist() default true;
}
