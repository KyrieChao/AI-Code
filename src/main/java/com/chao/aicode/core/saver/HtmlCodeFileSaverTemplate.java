package com.chao.aicode.core.saver;

import com.chao.aicode.ai.model.HtmlCodeResult;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存器
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected void saveFiles(HtmlCodeResult htmlCodeResult, String path) {
        writeToFile(path, "index.html", htmlCodeResult.getHtmlCode());
    }

    @Override
    public CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    public void validateInput(HtmlCodeResult htmlCodeResult) {
        super.validateInput(htmlCodeResult);
        if (htmlCodeResult.getHtmlCode() == null || htmlCodeResult.getHtmlCode().isEmpty()) {
            throw new BusinessException(HTTPResponseCode.SYSTEM_ERROR, "HTML代码不能为空");
        }
    }
}
