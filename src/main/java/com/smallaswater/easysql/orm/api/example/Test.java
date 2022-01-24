package com.smallaswater.easysql.orm.api.example;

import com.smallaswater.easysql.mysql.utils.Types;
import com.smallaswater.easysql.mysql.utils.UserData;
import com.smallaswater.easysql.orm.api.ORMHandleBuilder;
import com.smallaswater.easysql.orm.handle.ORMDynaProxyHandle;

import java.util.List;

public class Test {


    public static void main(String[] args) {

//        ExampleEntity entity = new ExampleEntity();
//        ORMHandleBuilder builder = ORMHandleBuilder.builder(null/*your pluginBase*/, "test");
//        ExampleDAO dao = ((ExampleDAO) builder.fromUserData(new UserData("root", "114514", "localhost", 3306, "test_db"))
//                .buildHost()
//                .buildORMDynaProxyIDAO(ExampleDAO.class));
//        dao.doSomething();

//        ORMDynaProxyHandle<ExampleDAO> handle = new ORMDynaProxyHandle<>(ExampleDAO.class, "t_table", null);
//
//        ExampleDAO idao = handle.getProxyInstance();
//        idao.doSomething();

        System.out.println(Types.INT);

    }
}
