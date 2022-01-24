package com.smallaswater.easysql.orm.internal;

import cn.nukkit.plugin.PluginClassLoader;
import com.smallaswater.easysql.mysql.data.SqlData;
import com.smallaswater.easysql.mysql.data.SqlDataList;
import com.smallaswater.easysql.mysql.utils.ChunkSqlType;
import com.smallaswater.easysql.orm.annotations.dao.DoDefault;
import com.smallaswater.easysql.orm.annotations.dao.DoExecute;
import com.smallaswater.easysql.orm.annotations.dao.DoInsert;
import com.smallaswater.easysql.orm.annotations.dao.DoQuery;
import com.smallaswater.easysql.orm.annotations.entity.*;
import com.smallaswater.easysql.orm.api.IDAO;
import com.smallaswater.easysql.v3.mysql.manager.SqlManager;
import javassist.*;

import java.lang.reflect.*;
import java.util.*;

public class ORMInvocation<T extends IDAO<?>> implements InvocationHandler {

    private final SqlManager manager;
    private final String table;
    private final Class<T> clazz; // 被代理的接口 class
    private final Class<?> entityClass;

    private final Class<?> defaultDoClass;

    private Object defaultDoProxyObj;

    // 储存 name -> Field 的 map
    private final Map<String, Field> columnFields = new HashMap<>();

    public ORMInvocation(Class<T> clazz, String table, SqlManager manager) {
        this.manager = manager;
        this.table = table;
        this.clazz = clazz;
        this.entityClass = ((Class<?>) ((ParameterizedType) Arrays.stream(clazz.getGenericInterfaces())
                .filter(t -> t.getTypeName().replaceAll("<\\S+>", "").endsWith("IDAO"))
                .findFirst().get()).getActualTypeArguments()[0]);
        this.defaultDoClass = generateDoDefaultClass();
        try {

            this.defaultDoProxyObj = this.defaultDoClass.newInstance();

            // 依赖注入
            Field m = this.defaultDoClass.getDeclaredField("manager");
            m.setAccessible(true);

            // this.manager 是 PluginClassLoader 加载的SqlManager类生成的对象
            // defaultDoProxyObj 是由 javassist 的类加载器加载的类生成的对象，其中的 manager 字段的类型的SqlManager类是由这个类加载器加载的(javassist中的类加载器打破了父类委托机制，即使把 PluginClassLoader 设置成 javassist 的父加载器, 它也不会让 PluginClassLoader 去加载)
            // 然后就不能把 this.manager 写入到 defaultDoProxyObj的 manager 字段里，因为它们的类不是同一个类加载器加载的，类型不同，蕨了，艹
            // 解决方式：自己写了一个 ASMClassloader，给 javassist 的类加载器重建父类委托机制
            m.set(this.defaultDoProxyObj, this.manager);

            Field t = this.defaultDoClass.getDeclaredField("table");
            t.setAccessible(true);
            t.set(this.defaultDoProxyObj, this.table);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Arrays.stream(entityClass.getDeclaredFields())
                .filter( t -> t.isAnnotationPresent(Column.class))
                .forEach( t -> {
                    Column c = t.getAnnotation(Column.class);
                    columnFields.put(c.name(), t);
                });
    }

    // 使用 javassist 生成执行 Default 的类(继承代理的接口类)
    @SuppressWarnings("unchecked")
    private Class<?> generateDoDefaultClass() {
        ClassPool classPool = ClassPool.getDefault();

        classPool.insertClassPath(new ClassClassPath(this.getClass()));

        String cn = this.clazz.getName() + "ImplClass";
        CtClass clazz = classPool.makeClass(cn);
        CtClass interf  = null;

        //Loader loader = new Loader(this.getClass().getClassLoader(), classPool); 爬
        // 使用自己的类加载器，重建父类委托机制
        ASMClassLoader loader = new ASMClassLoader((PluginClassLoader) this.getClass().getClassLoader(), classPool);

        try {
            interf = classPool.getCtClass(this.clazz.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        clazz.setInterfaces(new CtClass[]{interf});

        try {
            clazz.addField(CtField.make("public com.smallaswater.easysql.v3.mysql.manager.SqlManager manager;", clazz));
            clazz.addField(CtField.make("public java.lang.String table;", clazz));

            // 构造被拦截的方法，模拟拦截
            clazz.addMethod(CtMethod.make("public com.smallaswater.easysql.v3.mysql.manager.SqlManager getManager(){return manager;}", clazz));
            clazz.addMethod(CtMethod.make("public java.lang.String getTable(){return table;}", clazz));
            clazz.addMethod(CtMethod.make("public java.lang.String toString(){return \"Table: \" + table + \" Manager: \" + manager;}", clazz));

        } catch (Exception e) {
            e.printStackTrace();
        }


        Class<?> ret = null;
        try {

            // 强行把生成的类塞到 PluginClassLoader 里
            Field classesField = PluginClassLoader.class.getDeclaredField("classes");
            classesField.setAccessible(true);
            HashMap<String, Class<?>> classes = ((HashMap<String, Class<?>>) classesField.get(this.getClass().getClassLoader()));
            classes.put(cn, loader.loadClass(cn));
            classesField.set(this.getClass().getClassLoader(), classes);

            ret = this.getClass().getClassLoader().loadClass(cn);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private Field findField(String identifier) {
        return this.columnFields.get(identifier);
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
                method.setAccessible(true);
                ret = method.invoke(this.defaultDoProxyObj, args);
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
            return handleQuery(method.getAnnotation(DoQuery.class).value(), args);
        }
        return null;
    }


    private List<Object> handleQuery(String sql, Object[] args) {
        sql = sql.replace("{table}", this.table);
        if (args != null && !checkParams(sql, Objects.requireNonNull(args).length)) {
            throw new RuntimeException("SQL语法错误，入参数量不匹配");
        }
        ArrayList<ChunkSqlType> chunks = new ArrayList<>();
        int i = 1;
        String s = sql;
        while (s.contains("?")) {
            s = s.replace("?", "");
            ChunkSqlType chunk = new ChunkSqlType(i, args[i - 1].toString());
            chunks.add(chunk);
            i ++;
        }

        List<Object> ret = new ArrayList<>();
        SqlDataList<SqlData> queryData = this.manager.getData(sql, chunks.toArray(new ChunkSqlType[0]));
        for (SqlData data : queryData) {
            try {
                Object obj = this.entityClass.newInstance();

                for (Map.Entry<String, Object> entry : data.getData().entrySet()) {
                    String columnName = entry.getKey();
                    Object value = entry.getValue();
                    Field columnFiled = findField(columnName);
                    if (columnFiled != null) {
                        columnFiled.set(obj, value);
                    }
                }

                ret.add(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }



    private void handleInsert(Object[] args) {

    }

    private void handleExecute(String sql, Object[] args) {

    }


    private boolean checkParams(String sql, int cnt) {
         return cnt == sql.split("\\?").length - 1;
    }

}
