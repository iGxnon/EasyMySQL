package com.smallaswater.easysql.orm.api;


import com.smallaswater.easysql.orm.annotations.entity.Column;
import com.smallaswater.easysql.orm.annotations.entity.PrimaryKey;
import com.smallaswater.easysql.orm.utils.ColumnTypes;
import com.smallaswater.easysql.orm.utils.UniqueKeyTypes;

/**
 * 可以继承这个类，将以下字段内嵌入用户自定义的Entity内
 */
public class Entity {

    @PrimaryKey(type = UniqueKeyTypes.BIGINT)
    @Column(identifier = "id", type = ColumnTypes.BIGINT)
    public long id;

    // TODO 追踪用户操作
    //public Timestamp CreatedAt;
    //public Timestamp UpdatedAt;

}
