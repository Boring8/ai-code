package com.easen.aicode.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * 编辑事件 Disruptor 配置
 */
@Configuration
public class AppEventDisruptorConfig {

    @Resource
    private AppEventWorkHandler appEventWorkHandler;

    @Bean("appEventDisruptor")
    public Disruptor<AppEvent> messageModelRingBuffer() {
        // 定义 ringBuffer 的大小
        int bufferSize = 1024 * 256;
        // 创建 disruptor
        Disruptor<AppEvent> disruptor = new Disruptor<>(
                AppEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("appEventDisruptor").build()
        );
        // 设置消费者
        disruptor.handleEventsWithWorkerPool(appEventWorkHandler);
        // 启动 disruptor
        disruptor.start();
        return disruptor;
    }
}
