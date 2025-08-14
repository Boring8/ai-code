package com.easen.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.easen.aicode.ai.AiCodeGenNameService;
import com.easen.aicode.ai.AiCodeGenTypeRoutingService;
import com.easen.aicode.common.DeleteRequest;
import com.easen.aicode.constant.AppConstant;
import com.easen.aicode.core.AiCodeGeneratorFacade;
import com.easen.aicode.core.AppResourceCleaner;
import com.easen.aicode.core.builder.VueProjectBuilder;
import com.easen.aicode.core.hander.StreamHandlerExecutor;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.langgraph4j.CodeGenConcurrentWorkflow;
import com.easen.aicode.model.dto.app.AppAddRequest;
import com.easen.aicode.model.dto.app.AppQueryRequest;
import com.easen.aicode.model.entity.App;
import com.easen.aicode.mapper.AppMapper;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.easen.aicode.model.enums.CodeGenTypeEnum;
import com.easen.aicode.model.vo.AppVO;
import com.easen.aicode.service.*;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a>easen</a>
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private AppUserService appUserService;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    @Autowired
    private UserService userService;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private AppResourceCleaner appResourceCleaner;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser, boolean agent) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用
        // 个人应用：只有创建者可以访问
        // 团队应用：创建者和团队成员都可以访问
        if (!appUserService.hasAppPermission(appId, loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        log.info("chatToGenCode AI回复:  appId:{}", appId);
        // 5. 在调用 AI 前，先保存用户消息到数据库中
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId(), 0);
        // 6. 调用 AI 生成代码（流式）
        Flux<String> codeStream;
        if (agent){
            //会员
            codeStream = new CodeGenConcurrentWorkflow().executeWorkflowWithFlux(message, appId, loginUser.getId());
        }else {
            //普通
            codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId, loginUser.getId());
        }
        // 7. 收集 AI 响应的内容，并且在完成后保存记录到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);

    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验
        // 个人应用：只有创建者可以部署
        // 团队应用：创建者和团队成员都可以部署
        if (!appUserService.hasAppPermission(appId, loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 如果没有，则生成 6 位 deployKey（字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，获取原始代码生成路径（应用访问目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码路径不存在，请先生成应用");
        }
        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请重试");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 构建完成后，需要将构建后的文件复制到部署目录
            sourceDir = distDir;
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
        }
        // 9. 更新数据库
        App updateApp = new App();
        if (app.getAppName() == null) {
            // 生成名称
            String appName = aiCodeGenNameService.generateAppName(app.getInitPrompt());
            ThrowUtils.throwIf(appName == null || appName.length() > 8, ErrorCode.SYSTEM_ERROR, "AI 生成的名称不符合规范");
            updateApp.setAppName(appName);
        }
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 得到可访问的 URL 地址
        String appDeployUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11. 异步生成截图并且更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程并执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新数据库的封面
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    @Resource
    private AiCodeGenNameService aiCodeGenNameService;

    /**
     * 创建新应用
     *
     * @param appAddRequest 应用创建请求对象，包含应用的基本信息
     * @param request       HTTP请求对象，用于获取当前登录用户信息
     * @return 新创建的应用ID
     */
    @Override
    public String addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        // 1. 参数校验：检查请求对象是否为空
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 参数校验：检查初始化提示词是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()), ErrorCode.PARAMS_ERROR, "初始化提示词不能为空");

        // 3. 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);

        // 4. 创建应用实体对象
        App app = new App();
        // 5. 将请求对象属性复制到应用实体
        BeanUtil.copyProperties(appAddRequest, app);
        // 6. 设置应用创建者ID
        app.setUserId(loginUser.getId());

        // 7. 使用AI智能路由服务，根据初始化提示词自动选择代码生成类型
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(app.getInitPrompt());
        app.setCodeGenType(selectedCodeGenType.getValue());

        // 8. 保存应用到数据库
        boolean result = this.save(app);

        // 10. 检查保存操作是否成功
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 11. 返回新创建的应用ID
        return String.valueOf(app.getId());
    }

    /**
     * 删除应用及其相关数据
     *
     * @param appId 要删除的应用ID
     * @return 删除操作是否成功
     */
    @Override
    @Transactional
    public Boolean deleteApp(Long appId) {
        // 1. 参数校验：检查应用ID是否有效
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        // 2. 检查应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 删除应用相关的聊天记录
        boolean chatHistoryDeleted = chatHistoryService.deleteByAppId(appId);
        log.info("删除应用 {} 的聊天记录，结果：{}", appId, chatHistoryDeleted);

        // 4. 删除应用团队成员关联关系
        boolean teamMembersDeleted = appUserService.removeAllUsersFromApp(appId);
        log.info("删除应用 {} 的团队成员，结果：{}", appId, teamMembersDeleted);

        // 5. 删除应用本身
        boolean appDeleted = this.removeById(appId);
        log.info("删除应用 {}，结果：{}", appId, appDeleted);

        // 6. 使用 AppResourceCleaner 异步清理应用相关的文件资源
        appResourceCleaner.cleanupAppResourcesAsync(app);

        // 7. 返回删除操作结果
        return appDeleted;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream().map(this::getAppVO).collect(Collectors.toList());
    }

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String codeGenType = appQueryRequest.getCodeGenType();
        Long userId = appQueryRequest.getUserId();
        Integer priority = appQueryRequest.getPriority();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        Integer isPublic = appQueryRequest.getIsPublic();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id)
                .eq("codeGenType", codeGenType)
                .eq("userId", userId)
                .eq("priority", priority)
                .eq("isPublic", isPublic)
                .like("appName", appName);

        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderBy("createTime", false);
        }

        return queryWrapper;
    }

    @Override
    public boolean validateUserPermission(Long appId, Long userId) {
        if (appId == null || userId == null) {
            return false;
        }
        App app = this.getById(appId);
        if (app == null) {
            return false;
        }

        // 如果是创建者，直接有权限
        if (app.getUserId().equals(userId)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isAppCreator(Long appId, Long userId) {
        if (appId == null || userId == null) {
            return false;
        }
        App app = this.getById(appId);
        if (app == null) {
            return false;
        }
        return app.getUserId().equals(userId);
    }
}
