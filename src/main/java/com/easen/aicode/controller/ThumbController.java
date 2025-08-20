package com.easen.aicode.controller;


import com.easen.aicode.common.BaseResponse;
import com.easen.aicode.common.PageRequest;
import com.easen.aicode.common.ResultUtils;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.model.entity.Thumb;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.vo.AppThumbVO;
import com.easen.aicode.service.ThumbService;
import com.easen.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 *  控制层。
 *
 * @author <a>easen</a>
 */
@RestController
@RequestMapping("/thumb")
public class ThumbController {

    @Autowired
    private ThumbService thumbService;

    @Autowired
    private UserService userService;


    /**
     * 排行，按点赞数从高到低排序
     *
     * @return 分页结果，包含应用id、应用名称、点赞数
     */
    @GetMapping("/appThumbList")
    public BaseResponse<List<AppThumbVO>> appThumbList() {
        List<AppThumbVO> result = thumbService.appThumbList();
        return ResultUtils.success(result);
    }

    /**
     * 用户点赞应用
     *
     * @param appId   应用ID
     * @param request 请求对象
     * @return 点赞结果
     */
    @PostMapping("/like/{appId}")
    public BaseResponse<Boolean> likeApp(@PathVariable String appId, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null , ErrorCode.PARAMS_ERROR, "应用ID无效");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行点赞操作
        boolean success = thumbService.likeApp(Long.valueOf(appId), loginUser.getId());

        return ResultUtils.success(success);
    }

    /**
     * 用户取消点赞应用
     *
     * @param appId   应用ID
     * @param request 请求对象
     * @return 取消点赞结果
     */
    @DeleteMapping("/unlike/{appId}")
    public BaseResponse<Boolean> unlikeApp(@PathVariable String appId, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null , ErrorCode.PARAMS_ERROR, "应用ID无效");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行取消点赞操作
        boolean success = thumbService.unlikeApp(Long.valueOf(appId), loginUser.getId());
        
        return ResultUtils.success(success);
    }

    /**
     * 检查用户是否已点赞应用
     *
     * @param appId   应用ID
     * @param request 请求对象
     * @return 是否已点赞
     */
    @GetMapping("/isLiked/{appId}")
    public BaseResponse<Boolean> isUserLikedApp(@PathVariable String appId, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null , ErrorCode.PARAMS_ERROR, "应用ID无效");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 检查是否已点赞
        boolean isLiked = thumbService.isUserLikedApp(Long.valueOf(appId), loginUser.getId());

        return ResultUtils.success(isLiked);
    }

}
