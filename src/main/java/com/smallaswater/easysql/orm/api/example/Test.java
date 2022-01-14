package com.smallaswater.easysql.orm.api.example;


import com.smallaswater.easysql.orm.handle.ORMDynaProxyHandle;

public class Test {

    public static void main(String[] args) {
        ORMDynaProxyHandle<ExampleDAO> handle = new ORMDynaProxyHandle<>(ExampleDAO.class);
        ExampleDAO dao = handle.getProxyInstance();
        dao.insert(new ExampleEntity());
        System.out.println(dao);
    }
}
