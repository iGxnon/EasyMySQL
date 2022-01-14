package com.smallaswater.easysql.orm.handle;

import com.smallaswater.easysql.orm.api.IDAO;
import com.smallaswater.easysql.orm.internal.ORMInvocation;

import java.lang.reflect.Proxy;

public class ORMDynaProxyHandle<T extends IDAO<?>> implements ORMBaseHandle {

    private final Class<T> proxyClazz;

    public ORMDynaProxyHandle(Class<T> proxyClazz) {
        this.proxyClazz = proxyClazz;
    }

    @SuppressWarnings("unchecked")
    public T getProxyInstance() {
        return (T) Proxy.newProxyInstance(proxyClazz.getClassLoader(), new Class<?>[]{proxyClazz}, new ORMInvocation<T>());
    }
}
