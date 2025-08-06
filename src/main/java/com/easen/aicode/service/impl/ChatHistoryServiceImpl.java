package com.easen.aicode.service.impl;


import com.easen.aicode.mapper.ChatHistoryMapper;
import com.easen.aicode.model.entity.ChatHistory;
import com.easen.aicode.service.ChatHistoryService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a>easen</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService {

}
