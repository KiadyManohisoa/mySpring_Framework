package com.itu.myspringframework.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })

public @interface FieldParameter {
    String value() default "default value";
}
