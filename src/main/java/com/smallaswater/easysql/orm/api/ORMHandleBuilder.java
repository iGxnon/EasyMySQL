package com.smallaswater.easysql.orm.api;

import com.smallaswater.easysql.mysql.utils.UserData;
import com.smallaswater.easysql.orm.handle.ORMDynaProxyHandle;
import com.smallaswater.easysql.orm.handle.ORMStdHandle;

public class ORMHandleBuilder {

    private String user;

    private String passWorld;

    private String host;

    private int port;

    private String database;

    private static ORMHandleBuilder builder() {
        return new ORMHandleBuilder();
    }

    public ORMHandleBuilder fromUserData(UserData userData) {
        this.user = userData.getUser();
        this.passWorld = userData.getPassWorld();
        this.host = userData.getHost();
        this.port = userData.getPort();
        this.database = userData.getDatabase();
        return this;
    }

    public ORMHandleBuilder buildHost() {

        return this;
    }

    public ORMStdHandle buildStdHandle() {
        return new ORMStdHandle();
    }

    public <T> IDAO<T> buildORMDynaProxyIDAO(Class<IDAO<T>> clazz) {
        return new ORMDynaProxyHandle<>(clazz).getProxyInstance();
    }

}
