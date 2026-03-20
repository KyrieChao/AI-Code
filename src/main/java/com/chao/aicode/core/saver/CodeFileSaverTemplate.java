package com.chao.aicode.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 模板方法模式
 *
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {
    /**
     * 文件保存方法
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output/";

    /**
     * 模板方法 保存代码的标准流程
     *
     * @param t     输入参数
     * @param appId 应用 ID
     * @return 保存的目录对象
     */
    public final File saveCode(T t, Long appId) {
        // 1.验证输入
        validateInput(t);
        // 2.构建唯一目录
        String baseDirPath = buildUniqueDir(appId);
        // 3.保存文件(具体实现交给子类)
        saveFiles(t, baseDirPath);
        // 4.返回保存的目录对象
        return new File(baseDirPath);
    }

    /**
     * 验证输入参数 可由子类覆盖
     *
     * @param t 输入参数
     */
    protected void validateInput(T t) {
        if (t == null) {
            throw new BusinessException(HTTPResponseCode.SYSTEM_ERROR, "参数不能为空");
        }
    }

    /**
     * 保存单个文件
     *
     * @param dirPath  文件保存的目录
     * @param filename 文件名
     * @param content  文件内容
     */
    public final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 构建文件的唯一路径：tmp/code_output/bizType_雪花 ID
     *
     * @param appId 应用 ID
     * @return 唯一路径
     */
    protected String buildUniqueDir(Long appId) {
        if (appId == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "应用 ID 不能为空");
        }
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存文件 由子类实现具体地保存逻辑
     *
     * @param t    输入参数
     * @param path 保存路径
     */
    protected abstract void saveFiles(T t, String path);

    /**
     * 获取代码生成类型
     *
     * @return 代码生成类型
     */
    public abstract CodeGenTypeEnum getCodeType();
}
