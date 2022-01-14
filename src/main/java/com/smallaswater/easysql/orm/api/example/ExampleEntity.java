package com.smallaswater.easysql.orm.api.example;

import com.smallaswater.easysql.orm.annotations.entity.*;
import com.smallaswater.easysql.orm.utils.ColumnOptions;
import com.smallaswater.easysql.orm.utils.ColumnTypes;
import com.smallaswater.easysql.orm.utils.UniqueKeyTypes;

public class ExampleEntity {

    @PrimaryKey(type = UniqueKeyTypes.BIGINT)
    @AutoIncrement // 可以省略
    @Column(identifier = "id", type = ColumnTypes.BIGINT, options = {})
    public Long id;

    @UniqueKey(type = UniqueKeyTypes.UUID) // 和 @AutoUUIDGenerate 效果一样
    @AutoUUIDGenerate // 可以省略
    @Column(identifier = "uuid", type = ColumnTypes.VARCHAR, options = {})
    public String uuid;

    @AutoIncrement
    @Column(identifier = "register_index", type = ColumnTypes.BIGINT, options = {})
    public Long registerIndex;

    @AutoUUIDGenerate
    @Column(identifier = "second_uuid", type = ColumnTypes.BIGINT, options = {})
    public Long secondUUID;

    @Column(identifier = "name",
            type = ColumnTypes.VARCHAR,
            options = {ColumnOptions.NOTNULL})
    public String name;

}
