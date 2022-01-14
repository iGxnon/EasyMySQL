package com.smallaswater.easysql.orm.internal;

import com.smallaswater.easysql.orm.api.IDAO;
import com.smallaswater.easysql.v3.mysql.manager.SqlManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ORMInvocation<T extends IDAO<?>> implements InvocationHandler {

    private final SqlManager manager;
    private final String table;

    public ORMInvocation(String table, SqlManager manager) {
        this.manager = manager;
        this.table = table;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("getManager")) {
            return this.manager;
        }

        if (method.getName().equals("getTable")) {
            return this.table;
        }

        System.out.println("begin");
        Object ret = null;
        System.out.println("end");
        return ret;
    }


}
