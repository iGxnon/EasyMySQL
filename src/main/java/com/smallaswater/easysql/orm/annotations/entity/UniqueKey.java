package com.smallaswater.easysql.orm.annotations.entity;

import com.smallaswater.easysql.orm.utils.UniqueKeyTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface UniqueKey {
    UniqueKeyTypes type();
}
