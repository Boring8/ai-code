package com.easen.aicode.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 热键缓存注解
 * 用于标记需要热键缓存的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HotKeyCache {
    
    /**
     * 缓存键的前缀
     */
    String prefix() default "";
    
    /**
     * 缓存键的参数索引，用于从方法参数中获取键值
     * 默认使用第一个参数
     */
    int keyParamIndex() default 0;
    
    /**
     * 缓存键的字段名，如果参数是对象，可以指定对象的字段名
     * 为空时直接使用参数值作为键
     */
    String keyField() default "";
    
    /**
     * 是否启用热键检测
     */
    boolean enableHotKey() default true;
    
    /**
     * 缓存过期时间（秒），0表示不过期
     */
    long expireSeconds() default 0;
    
    /**
     * 是否缓存空值
     */
    boolean cacheNull() default false;
}
