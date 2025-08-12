package com.easen.aicode.controller;

import com.easen.aicode.annotation.AuthCheck;
import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.constant.UserConstant;

import com.easen.aicode.manager.auth.annotation.SaSpaceCheckPermission;
import com.easen.aicode.manager.auth.model.AppUserPermissionConstant;
import com.easen.aicode.model.dto.ChatHistoryQueryRequest;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.ChatHistoryVO;
import com.easen.aicode.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import com.easen.aicode.service.UserService;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author <a>easen</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @return 对话历史分页
     */
    @GetMapping("/app")
    @SaSpaceCheckPermission(value = AppUserPermissionConstant.APP_VIEW)
    public BaseResponse<Page<ChatHistoryVO>> listAppChatHistory(@RequestParam Long appId,
                                                                @RequestParam(defaultValue = "10") int pageSize,
                                                                @RequestParam(required = false) LocalDateTime lastCreateTime
    ) {
        Page<ChatHistoryVO> result = chatHistoryService.listAppChatHistoryVOByPage(appId, pageSize, lastCreateTime);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        Page<ChatHistoryVO> result = chatHistoryService.listAllChatHistoryVOByPageForAdmin(chatHistoryQueryRequest);
        return ResultUtils.success(result);
    }

}
