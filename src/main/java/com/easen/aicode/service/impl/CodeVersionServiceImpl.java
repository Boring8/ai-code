package com.easen.aicode.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.exception.ThrowUtils;
import com.easen.aicode.mapper.CodeVersionMapper;
import com.easen.aicode.model.entity.CodeVersion;
import com.easen.aicode.service.CodeVersionService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 代码版本 服务层实现。
 */
@Slf4j
@Service
public class CodeVersionServiceImpl extends ServiceImpl<CodeVersionMapper, CodeVersion> implements CodeVersionService {

    @Override
    public boolean addCodeVersion(Long appId, String codeGenType, String content, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(codeGenType), ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // content 允许为空（比如生成被取消/异常时也可按需写入），这里先不强校验

        CodeVersion version = CodeVersion.builder()
                .appId(appId)
                .codeGenType(codeGenType)
                .content(content == null ? "" : content)
                .userId(userId)
                .build();
        return this.save(version);
    }

    @Override
    public String getLatestContent(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        QueryWrapper query = QueryWrapper.create()
                .eq(CodeVersion::getAppId, appId)
                .orderBy(CodeVersion::getCreateTime, false)
                // MyBatis-Flex: limit(offset, pageSize)
                .limit(1, 1);
        CodeVersion latest = this.getOne(query);
        if (latest == null || latest.getContent() == null) {
            return "";
        }
        return latest.getContent();
    }
}

