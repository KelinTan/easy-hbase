package com.gaoxin.mop.annotation;

import java.lang.annotation.*;

/**
 * Author: Mr.tan
 * Date:  2017/08/18
 * <p>
 * used on the field associate to the column from the hbase
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HBaseColumn {

    String family() default "";

    String column();

    boolean exist() default true;
}
