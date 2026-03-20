package com.chao.aicode.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.ThrowUtils;
import com.chao.aicode.service.ScreenshotService;
import com.chao.aicode.util.WebScreenshotUtils;
import com.chao.aicode.manager.AliYun;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private AliYun Yun;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), HTTPResponseCode.PARAM_ERROR, "截图的网址不能为空");
        log.info("开始生成网页截图，URL：{}", webUrl);
        // 本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), HTTPResponseCode.SYSTEM_ERROR, "生成网页截图失败");
        // 上传图片到 COS
        try {
            String cosUrl = uploadScreenshotToOos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), HTTPResponseCode.SYSTEM_ERROR, "上传截图到对象存储失败");
            log.info("截图上传成功，URL：{}", cosUrl);
            return cosUrl;
        } finally {
            // 清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToOos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        InputStream inputStream = FileUtil.getInputStream(screenshotFile);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 生成 COS 对象键
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = Yun.imagePath() + fileName;
        return Yun.uploadImageIfValid(cosKey, inputStream, screenshotFile.length());
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            FileUtil.del(localFile);
            log.info("清理本地文件成功: {}", localFilePath);
        }
    }
}
