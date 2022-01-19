package com.smallaswater.easysql.orm.annotations.entity;

import com.smallaswater.easysql.orm.utils.ColumnOptions;
import com.smallaswater.easysql.orm.utils.ColumnTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    String identifier();

    ColumnTypes type();

    ColumnOptions[] options() default {};

}
