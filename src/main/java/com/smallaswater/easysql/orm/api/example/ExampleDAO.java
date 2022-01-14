package com.smallaswater.easysql.orm.api.example;

import com.smallaswater.easysql.orm.annotations.dao.DoInsert;
import com.smallaswater.easysql.orm.api.IDAO;

public interface ExampleDAO extends IDAO<ExampleEntity> {

    @DoInsert
    public void insert(ExampleEntity entity);

}
