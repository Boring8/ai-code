package com.easen.aicode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.easen.aicode.ai.model.HtmlCodeResult;
import com.easen.aicode.core.anchorPoint.HtmlStableAnchorInjector;
import com.easen.aicode.exception.BusinessException;
import com.easen.aicode.exception.ErrorCode;
import com.easen.aicode.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存器
 *
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 保存 HTML 文件
        String htmlWithAnchors = HtmlStableAnchorInjector.injectStableAnchors(result.getHtmlCode());
        writeToFile(baseDirPath, "index.html", htmlWithAnchors);
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // HTML 代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}
