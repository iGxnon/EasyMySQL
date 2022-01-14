package com.smallaswater.easysql.orm.handle;

import com.smallaswater.easysql.orm.api.IDAO;
import com.smallaswater.easysql.orm.internal.ORMInvocation;
import com.smallaswater.easysql.v3.mysql.manager.SqlManager;

import java.lang.reflect.Proxy;

public class ORMDynaProxyHandle<T extends IDAO<?>> implements ORMBaseHandle {

    private final Class<T> proxyClazz;
    private final SqlManager sqlManager;
    private final String table;

    public ORMDynaProxyHandle(Class<T> proxyClazz, String table, SqlManager sqlManager) {
        this.proxyClazz = proxyClazz;
        this.sqlManager = sqlManager;
        this.table = table;
    }

    @SuppressWarnings("unchecked")
    public T getProxyInstance() {
        return (T) Proxy.newProxyInstance(proxyClazz.getClassLoader(), new Class<?>[]{proxyClazz}, new ORMInvocation<T>(table, sqlManager));
    }
}
