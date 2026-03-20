package com.chao.aicode.manager;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class AliYun {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    @Value("${aliyun.oss.projectBucket}")
    private String projectBucket;
    @Value("${aliyun.oss.imageBucket}")
    private String imageBucket;
    @Value("${aliyun.oss.avatarBucket}")
    private String avatarBucket;
    private static final Set<String> ALLOW_SUFFIX = Set.of(".jpg", ".jpeg", ".png", ".webp", ".svg");
    private static final Set<String> ALLOW_MIME = Set.of("image/jpeg", "image/png", "image/webp", "image/svg+xml");

    @Value("${aliyun.oss.max-size}")
    private Long MAX_SIZE;

    private OSS createOssClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 上传文件
     *
     * @param objectKey   文件路径
     * @param inputStream 文件输入流
     * @return 文件访问 URL
     */
    private String upload(String objectKey, InputStream inputStream) {
        OSS ossClient = createOssClient();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream);
            ossClient.putObject(putObjectRequest);

            return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectKey;
        } catch (Exception e) {
            log.error("OSS文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public String imagePath() {
        return projectBucket + "/" + imageBucket + "/";
    }

    public String avatarPath() {
        return projectBucket + "/" + avatarBucket + "/";
    }

    /**
     * 通用图片上传（流式）
     *
     * @param objectKey   OSS 中的 key（含后缀）
     * @param inputStream 图片二进制流
     * @return 成功返回 OSS 访问 URL；任何校验失败返回 null
     */
    public String uploadImageIfValid(String objectKey, InputStream inputStream, long size) {
        if (size > MAX_SIZE) {
            log.warn("图片大小超限：{} bytes，上限：{}", size, MAX_SIZE);
            return null;
        }
        // OSS ObjectKey 不能以 / 开头
        if (objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
            log.warn("图片 OSS ObjectKey 不能以 / 开头：{}", objectKey);
        }
        String suffix = Optional.of(objectKey)
                .filter(f -> f.lastIndexOf('.') != -1)
                .map(f -> f.substring(f.lastIndexOf('.')).toLowerCase())
                .orElse("");
        if (!ALLOW_SUFFIX.contains(suffix)) {
            log.warn("图片后缀不合法：{}", objectKey);
            return null;
        }
        try (BufferedInputStream buffered = new BufferedInputStream(inputStream)) {
            buffered.mark(1024 * 1024);
            String mimeType = new Tika().detect(buffered);
            buffered.reset();
            if (!ALLOW_MIME.contains(mimeType)) {
                log.warn("图片 MIME 不合法：{}", mimeType);
                return null;
            }
            return upload(objectKey, buffered);
        } catch (IOException e) {
            log.error("图片流读取失败", e);
            return null;
        }
    }

    /**
     * 删除对象
     *
     * @param objectKey 要删除的对象 key
     */
    public void delete(String objectKey) {
        OSS ossClient = createOssClient();
        try {
            ossClient.deleteObject(bucketName, objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 从文件 URL 中提取 objectKey
     *
     * @param fileUrl 文件 URL
     * @return objectKey
     */
    public String extractObjectKey(String fileUrl) {
        String host = "https://" + bucketName + "." + endpoint.replace("https://", "") + "/";
        return fileUrl.replace(host, "");
    }

}
