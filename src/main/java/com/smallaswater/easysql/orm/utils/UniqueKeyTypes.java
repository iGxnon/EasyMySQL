package com.smallaswater.easysql.orm.utils;

public enum UniqueKeyTypes {
    /**
     * UUID 字符串类型的唯一键
     * 会自动生成保证唯一性
     */
    UUID,

    /**
     * 长整型的唯一键
     * 会自动使用自增保证唯一性
     */
    BIGINT,

    /**
     * 自动检测
     * 不会采用任何措施，需要自行保证唯一性
     */
    DETECT;

}
