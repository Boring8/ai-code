package com.easen.aicode.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.service.UserService;
import com.easen.aicode.websocket.AppEditHandler;
import com.easen.aicode.websocket.model.AppMessageTypeEnum;
import com.easen.aicode.websocket.model.AppRequestMessage;
import com.easen.aicode.websocket.model.AppResponseMessage;
import com.lmax.disruptor.WorkHandler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;



/**
 * 图片编辑事件处理器（消费者）
 */
@Component
@Slf4j
public class AppEventWorkHandler implements WorkHandler<AppEvent> {

    @Resource
    private AppEditHandler appHandler;

    @Resource
    private UserService userService;

    @Override
    public void onEvent(AppEvent appEvent) throws Exception {
        AppRequestMessage appRequestMessage = appEvent.getAppRequestMessage();
        WebSocketSession session = appEvent.getSession();
        User user = appEvent.getUser();
        Long appId = appEvent.getAppId();
        // 获取到消息类别
        String type = appRequestMessage.getType();
        AppMessageTypeEnum appMessageTypeEnum = AppMessageTypeEnum.getEnumByValue(type);
        // 根据消息类型处理消息
        switch (appMessageTypeEnum) {
            case USER_ENTER_EDIT:
                appHandler.userHandleEnterEditMessage(appRequestMessage, session, user, appId);
                break;
            case USER_EXIT_EDIT:
                appHandler.userHandleExitEditMessage(appRequestMessage, session, user, appId);
                break;
            case AI_EDIT_ACTION:
                appHandler.aiHandleEditActionMessage(appRequestMessage, session, user, appId);
                break;
//            case USER_EDIT_ACTION:
//                appHandler.userHandleEditActionMessage(appRequestMessage, session, user, appId);
//                break;
            default:
                // 其他消息类型，返回错误提示
                AppResponseMessage appResponseMessage = new AppResponseMessage();
                appResponseMessage.setType(AppMessageTypeEnum.ERROR.getValue());
                appResponseMessage.setMessage("消息类型错误");
                appResponseMessage.setUser(userService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(appResponseMessage)));
                break;
        }
    }
}
