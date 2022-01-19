package com.smallaswater.easysql.orm.api.example;

import com.smallaswater.easysql.orm.annotations.dao.DoDefault;

/**
 * 建议单独写一个接口 DefaultDo 来定义 @DoDefault 方法
 * 然后创建一个内部静态内去实现这个接口
 */
public interface DefaultDo {
    @DoDefault
    void doSomething();
}
