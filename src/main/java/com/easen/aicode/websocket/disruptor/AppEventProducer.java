package com.easen.aicode.websocket.disruptor;


import com.easen.aicode.model.entity.User;
import com.easen.aicode.websocket.model.AppRequestMessage;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;



/**
 * 图片编辑事件生产者
 */
@Component
@Slf4j
public class AppEventProducer {

    @Resource
    private Disruptor<AppEvent> appEventDisruptor;

    /**
     * 发布事件
     *
     * @param appRequestMessage
     * @param session
     * @param user
     * @param appId
     */
    public void publishEvent(AppRequestMessage appRequestMessage, WebSocketSession session, User user, Long appId) {
        RingBuffer<AppEvent> ringBuffer = appEventDisruptor.getRingBuffer();
        // 获取到可以防止事件的位置
        long next = ringBuffer.next();
        AppEvent pictureEditEvent = ringBuffer.get(next);
        pictureEditEvent.setAppRequestMessage(appRequestMessage);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setAppId(appId);
        // 发布事件
        ringBuffer.publish(next);
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void destroy() {
        appEventDisruptor.shutdown();
    }
}

