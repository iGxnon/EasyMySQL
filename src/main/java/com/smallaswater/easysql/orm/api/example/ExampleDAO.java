package com.smallaswater.easysql.orm.api.example;

import com.smallaswater.easysql.orm.annotations.dao.DoDefault;
import com.smallaswater.easysql.orm.annotations.dao.DoExecute;
import com.smallaswater.easysql.orm.annotations.dao.DoInsert;
import com.smallaswater.easysql.orm.annotations.dao.DoQuery;
import com.smallaswater.easysql.orm.api.IDAO;
import com.smallaswater.easysql.v3.mysql.manager.SqlManager;

import java.util.List;


public interface ExampleDAO extends IDAO<ExampleEntity> {

    @DoInsert
    void insert(ExampleEntity entity);

    @DoQuery("SELECT * FROM {table}") // {table} 会在执行时替换成Builder内的table
    List<ExampleEntity> queryEntities();

    @DoQuery("SELECT WHERE id > {0} FROM {table}") // 使用 {0} {1} 分别代表第一个和第二个参数  .toString()
    List<ExampleEntity> queryEntitiesIDAfter(Long id);

    @DoExecute("DELETE FROM {table} WHERE id = {1}")
    void deleteEntity(Long id);

    @DoDefault
    default void doOtherThings() {
        SqlManager manager = this.getManager();
        String table = this.getTable();
        // do with manager and table
    }

}
