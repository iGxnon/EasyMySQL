package com.smallaswater.easysql.orm.internal;

import cn.nukkit.plugin.PluginClassLoader;
import javassist.ClassPool;
import javassist.Loader;

import java.lang.reflect.Method;

// 为 javassist 的类加载器 Loader 重建父类委托机制
public class ASMClassLoader extends Loader {

    private final PluginClassLoader parent;

    public ASMClassLoader(PluginClassLoader parent, ClassPool classPool) {
        super(parent, classPool);
        this.parent = parent;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            // 让我访问!
            Method findClass = PluginClassLoader.class.getDeclaredMethod("findClass", String.class);
            findClass.setAccessible(true);
            Class<?> find = (Class<?>) findClass.invoke(this.parent, name);
            if (find != null) {
                return find;
            }
        } catch (Exception ignored) {

        }
        return super.findClass(name);
    }
}
