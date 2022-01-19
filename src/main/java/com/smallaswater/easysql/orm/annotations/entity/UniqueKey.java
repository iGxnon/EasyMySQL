package com.smallaswater.easysql.orm.annotations.entity;

import com.smallaswater.easysql.orm.utils.UniqueKeyTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UniqueKey {
    UniqueKeyTypes type();
}
