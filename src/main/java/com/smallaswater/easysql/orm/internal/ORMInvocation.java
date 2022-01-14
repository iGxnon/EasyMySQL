package com.smallaswater.easysql.orm.internal;

import com.smallaswater.easysql.orm.api.IDAO;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ORMInvocation<T extends IDAO<?>> implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("toString")) {
            return this.toString();
        }
        System.out.println("begin");
        Object ret = null;
        System.out.println("end");
        return ret;
    }


}
