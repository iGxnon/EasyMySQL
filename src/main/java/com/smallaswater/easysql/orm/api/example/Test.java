package com.smallaswater.easysql.orm.api.example;

import com.smallaswater.easysql.EasySql;
import com.smallaswater.easysql.mysql.utils.UserData;
import com.smallaswater.easysql.orm.api.ORMHandleBuilder;

import java.util.List;

public class Test {


    public static void main(String[] args) {

        ExampleEntity entity = new ExampleEntity();
        ORMHandleBuilder builder = ORMHandleBuilder.builder(null/*your pluginBase*/, "test");
        ExampleDAO dao = ((ExampleDAO) builder.fromUserData(new UserData("root", "114514", "localhost", 3306, "test_db"))
                .buildHost()
                .buildORMDynaProxyIDAO(ExampleDAO.class));
        dao.insert(entity); // 还不支持
        dao.deleteEntity(entity.id, "uuid");
        List<ExampleEntity> entities = dao.queryEntities();
        List<ExampleEntity> after = dao.queryEntitiesIDAfter(5L);

    }
}
