package com.smallaswater.easysql.orm.api.example;

import com.smallaswater.easysql.orm.annotations.dao.DoExecute;
import com.smallaswater.easysql.orm.annotations.dao.DoInsert;
import com.smallaswater.easysql.orm.annotations.dao.DoQuery;
import com.smallaswater.easysql.orm.api.IDAO;

import java.util.List;


public interface ExampleDAO extends IDAO<ExampleEntity>, DefaultDo {

    @DoInsert
    void insert(ExampleEntity entity);

    @DoQuery("SELECT * FROM {table}") // {table} 会在执行时替换成Builder内的table
    List<ExampleEntity> queryEntities();

    /**
     * 使用 {0} {1} 分别代表第一个和第二个参数
     * 不要出现间断，例如 {0}, {1}, {3} 这种
     */
    @DoQuery("SELECT WHERE id > {0} FROM {table}")
    List<ExampleEntity> queryEntitiesIDAfter(Long id);

    @DoExecute("DELETE FROM {table} WHERE id = {0} AND uuid = {1}")
    void deleteEntity(Long id, String uuid);

    /**
     * 由于接口无法实例化，必须找一个类来代理完成这个方法
     * 实际上调用的是 Default 对象的 同名同参同返回的方法
     *
     * 使用了 @DoDefault 必须要构造一个类 , 如下 class Default
     * 这个类里面的方法一定要和 @DoDefault 一样
     */
//    @Override
//    @DoDefault
//    void doSomething();  父接口已有，可以省略

    // 命名要以 Default 结尾
    class Default implements DefaultDo {

        @Override
        public void doSomething() {
            System.out.println("hello");
            // do with manager and table
        }
    }

}
