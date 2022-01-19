package com.smallaswater.easysql.orm.internal;


import com.smallaswater.easysql.orm.annotations.dao.DoDefault;
import com.smallaswater.easysql.orm.annotations.dao.DoExecute;
import com.smallaswater.easysql.orm.annotations.dao.DoInsert;
import com.smallaswater.easysql.orm.annotations.dao.DoQuery;
import com.smallaswater.easysql.orm.annotations.entity.*;
import com.smallaswater.easysql.orm.api.IDAO;
import com.smallaswater.easysql.orm.utils.ColumnOptions;
import com.smallaswater.easysql.orm.utils.ColumnTypes;
import com.smallaswater.easysql.v3.mysql.manager.SqlManager;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ORMInvocation<T extends IDAO<?>> implements InvocationHandler {

    private final SqlManager manager;
    private final String table;
    private final Class<T> clazz;
    private final Class<?> entityClass;

    private final Class<?> defaultDoClass;

    public Object defaultDoProxyObj;

    public ORMInvocation(Class<T> clazz, String table, SqlManager manager) {
        this.manager = manager;
        this.table = table;
        this.clazz = clazz;
        this.entityClass = ((Class<?>) ((ParameterizedType) Arrays.stream(clazz.getGenericInterfaces())
                .filter(t -> t.getTypeName().replaceAll("<\\S+>", "").endsWith("IDAO"))
                .findFirst().get()).getActualTypeArguments()[0]);
        this.defaultDoClass = Arrays.stream(clazz.getDeclaredClasses()).filter( t -> t.getName().endsWith("Default")).findFirst().get();
        try {
            this.defaultDoProxyObj = this.defaultDoClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 返回两个默认的方法
        if (method.getName().equals("getManager")) {
            return this.manager;
        }

        if (method.getName().equals("getTable")) {
            return this.table;
        }

        if (method.isAnnotationPresent(DoDefault.class)) {
            Object ret = null;
            try {
                Method proxyM = Arrays.stream(this.defaultDoClass.getMethods())
                        .filter(t -> t.getName().equals(method.getName())
                                && t.getGenericReturnType().getTypeName().equals(method.getGenericReturnType().getTypeName()))
                        .findFirst().get();
                proxyM.setAccessible(true);
                ret = proxyM.invoke(this.defaultDoProxyObj, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        }

        return handleMethod(method, args);
    }

    private Object handleMethod(Method method, Object[] args) {
        method.setAccessible(true);

        if (method.isAnnotationPresent(DoInsert.class)) {
            if (method.getParameterCount() != 1 && !method.getParameterTypes()[0].getName().equals(this.entityClass.getName())) {
                throw new RuntimeException("@DoInsert 修饰的方法只能有一个参数并且为范型类型!");
            }
            handleInsert(args);
            return null;
        }else if (method.isAnnotationPresent(DoExecute.class)) {
            if (!method.getGenericReturnType().getTypeName().equals("void")) {
                throw new RuntimeException("@DoExecute 修饰的方法不能有返回值!");
            }
            handleExecute(method.getAnnotation(DoExecute.class).value(), args);
            return null; // Execute 不支持返回
        }else if (method.isAnnotationPresent(DoQuery.class)) {
            return handleQuery(method.getAnnotation(DoExecute.class).value(), args);
        }
        return null;
    }


    private List<Object> handleQuery(String sql, Object[] args) {
        List<Object> result = new ArrayList<>();
        Pattern p = Pattern.compile("\\{\\d}");
        Matcher matcher = p.matcher(sql);
        int i = 0;
        List<String> params = new ArrayList<>();
        while (matcher.find()) {
            int index = Integer.parseInt(sql.substring(matcher.start() + 1, matcher.end() - 1));
            params.add(index, args[i].toString());
            sql = matcher.replaceFirst("?");
            matcher = p.matcher(sql);
            i ++;
        }
        sql = sql.replace("{table}", this.table);

        Connection connection = this.manager.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            for (int j = 0; j < params.size(); j++) {
                stmt.setString(j, params.get(j));
            }
            ResultSet ret = stmt.executeQuery();
            if (ret != null) {
                ResultSetMetaData data;
                while (ret.next()) {
                    data = ret.getMetaData();
                    Object entity = entityClass.newInstance();

                    for (i = 0; i < data.getColumnCount(); i++) {
                        String identifier = data.getColumnName(i + 1).toLowerCase(Locale.ROOT);
                        Object obj = ret.getObject(i + 1);

                        //  根据 identifier 从entityClass找到字段并把 obj cast 成该字段类型 然后写入 entity
                        Field field = this.findField(identifier);

                        Object to = field.getType().cast(obj);
                        field.set(entity, to);
                    }
                    result.add(entity);
                }
                ret.close();
            }
            stmt.close();
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } finally { // 及时释放资源
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    // TODO 将 identifier -> Field 注册进 Map
    private Field findField(String identifier) {
        AtomicReference<Field> find = new AtomicReference<>();
        Arrays.stream(entityClass.getDeclaredFields())
                .filter( t -> t.getAnnotations().length != 0) // 需要注解数不等于 0 的字段
                .filter( t -> t.isAnnotationPresent(Column.class)) // 得要有 Column 注解
                .forEach( t -> {
                    Column a = t.getAnnotation(Column.class);
                    if (identifier.equals(a.identifier())) {
                        find.set(t);
                    }
                });
        return find.get();
    }

    private void handleInsert(Object[] args) {
        Object paramEntity = entityClass.cast(args[0]);

        Arrays.stream(entityClass.getDeclaredFields())
                .filter( t -> t.getAnnotations().length != 0) // 需要注解数不等于 0 的字段
                .filter( t -> t.isAnnotationPresent(Column.class)) // 得要有 Column 注解
                .forEach( t -> {
                    try {
                        Object value = t.get(paramEntity); // 这个字段的值
                        if (t.isAnnotationPresent(PrimaryKey.class)) {
                            PrimaryKey annotation = t.getAnnotation(PrimaryKey.class);
                            switch (annotation.type()) {
                                case UUID:
                                    value = UUID.randomUUID().toString(); // 自动生成 uuid
                                    break;
                                case BIGINT:
                                    value = null; // 留给 mysql 自己补充
                                    break;
                            }
                        }else if (t.isAnnotationPresent(UniqueKey.class)) {
                            UniqueKey annotation = t.getAnnotation(UniqueKey.class);
                            switch (annotation.type()) {
                                case UUID:
                                    value = UUID.randomUUID().toString(); // 自动生成 uuid
                                    break;
                                case BIGINT:
                                    value = null; // 留给 mysql 自己补充
                                    break;
                            }
                        }

                        if (t.isAnnotationPresent(AutoUUIDGenerate.class)) {
                            value = UUID.randomUUID().toString(); // 自动生成 uuid
                        }else if (t.isAnnotationPresent(AutoIncrement.class)) {
                            value = null; // 留给 mysql 自己补充
                        }

                        Column c = t.getAnnotation(Column.class);
                        String identifier = c.identifier();
                        ColumnTypes type = c.type();
                        ColumnOptions[] options = c.options();

                        // TODO

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
        if (this.manager.isExistTable(this.table)) {
            // TODO 创建表
        }
        // TODO 插入值
    }

    private void handleExecute(String sql, Object[] args) {
        Pattern p = Pattern.compile("\\{\\d}");
        Matcher matcher = p.matcher(sql);
        int i = 0;
        List<String> params = new ArrayList<>();
        while (matcher.find()) {
            int index = Integer.parseInt(sql.substring(matcher.start() + 1, matcher.end() - 1));
            params.add(index, args[i].toString());
            sql = matcher.replaceFirst("?");
            matcher = p.matcher(sql);
            i ++;
        }
        sql = sql.replace("{table}", this.table);

        Connection connection = this.manager.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            for (int j = 0; j < params.size(); j++) {
                stmt.setString(j, params.get(j));
            }
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { // 及时释放资源
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
