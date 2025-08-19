package com.easen.aicode.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * HotKey 模块启用配置
 * 确保 hotkey 相关的组件被正确扫描和启用
 *
 * @author easen
 */
@Configuration
@ComponentScan(basePackages = {
        "com.easen.aicode.hotkey.aspect",
        "com.easen.aicode.hotkey"
})
public class HotKeyEnableConfig {
    
    // 配置类，用于启用 hotkey 模块
    // 通过 ComponentScan 确保 hotkey 相关的切面和配置被正确加载
}
