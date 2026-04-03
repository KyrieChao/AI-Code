package com.chao.aicode.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.chao.aicode.annotation.AuthCheck;
import com.chao.aicode.common.constants.AppConstant;
import com.chao.aicode.common.constants.UserConstant;
import com.chao.aicode.common.request.DeleteRequest;
import com.chao.aicode.common.response.ApiResponse;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.ThrowUtils;
import com.chao.aicode.model.dto.app.*;
import com.chao.aicode.model.entity.App;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.model.vo.AppVO;
import com.chao.aicode.ratelimter.annotation.RateLimit;
import com.chao.aicode.ratelimter.enums.RateLimitType;
import com.chao.aicode.service.AppService;
import com.chao.aicode.service.ProjectDownloadService;
import com.chao.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
@RestController
@RequestMapping("/app")
@AllArgsConstructor
public class AppController {

    private final AppService appService;
    private final UserService userService;
    private final ProjectDownloadService projectDownloadService;

    /**
     * 分页查询 应用列表
     *
     * @param appId   应用ID
     * @param message 提示信息
     * @param request 请求
     * @return 应用列表vo
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 60, message = "AI 对话请求过于频繁，请稍后再试")
    public Flux<ServerSentEvent<String>> chatGenCode(@RequestParam Long appId, @RequestParam String message, HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, HTTPResponseCode.PARAM_ERROR, "appId不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), HTTPResponseCode.PARAM_ERROR, "prompt不能为空");

        Flux<String> contentFlux = appService.chatToGenCode(appId, message, userService.getLoginUser(request));
        return contentFlux.map(chunk -> {
                    Map<String, String> per = Map.of("d", chunk);
                    String json = JSONUtil.toJsonStr(per);
                    return ServerSentEvent.<String>builder()
                            .data(json)
                            .build();
                })
                // 发送结束事件
                .concatWith(
                        Mono.just(
                                ServerSentEvent.<String>builder()
                                        .event("done")
                                        .data("")
                                        .build()));
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public ApiResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        // 检查部署请求是否为空
        ThrowUtils.throwIf(appDeployRequest.getAppId() == null, HTTPResponseCode.PARAM_ERROR);
        // 获取应用 ID
        Long appId = appDeployRequest.getAppId();
        // 检查应用 ID 是否为空
        ThrowUtils.throwIf(appId == null || appId <= 0, HTTPResponseCode.PARAM_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deploy(appId, loginUser);
        // 返回部署 URL
        return ApiResponse.success(deployUrl);
    }

    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId, HttpServletRequest request, HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, HTTPResponseCode.PARAM_ERROR, "应用ID无效");
        // 2. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        // 3. 权限校验：只有应用创建者可以下载代码
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), HTTPResponseCode.PERMISSION_DENIED, "无权限下载该应用代码");

        // 4. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 5. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), HTTPResponseCode.RESOURCE_NOT_FOUND, "应用代码不存在，请先生成代码");
        // 6. 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 7. 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }

    /**
     * 创建应用
     */
    @PostMapping("/add")
    public ApiResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest.getInitPrompt() == null, HTTPResponseCode.PARAM_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()), HTTPResponseCode.PARAM_ERROR, "initPrompt不能为空");
        User loginUser = userService.getLoginUser(request);
        Long appId = appService.createApp(appAddRequest, loginUser);
        return ApiResponse.success(appId);
    }

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     */
    @PostMapping("/update/my")
    public ApiResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest.getAppName() == null, HTTPResponseCode.PARAM_ERROR);
        ThrowUtils.throwIf(appUpdateRequest.getId() == null, HTTPResponseCode.PARAM_ERROR);
        Boolean result = appService.updateMyApp(appUpdateRequest, request);
        return ApiResponse.success(result);
    }

    /**
     * 根据 id 删除自己的应用
     */
    @PostMapping("/delete/my")
    public ApiResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest.getId() == null, HTTPResponseCode.PARAM_ERROR);
        Boolean result = appService.deleteMyApp(deleteRequest.getId(), request);
        return ApiResponse.success(result);
    }

    /**
     * 根据 id 查看应用详情
     */
    @GetMapping("/get/vo")
    public ApiResponse<AppVO> getAppVOById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, HTTPResponseCode.PARAM_ERROR);
        AppVO appVO = appService.getAppVOById(id);
        return ApiResponse.success(appVO);
    }

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多20个）
     */
    @PostMapping("/list/my/page/vo")
    public ApiResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest queryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, HTTPResponseCode.PARAM_ERROR);
        Page<AppVO> appVOPage = appService.listMyAppByPage(queryRequest, request);
        return ApiResponse.success(appVOPage);
    }

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多20个）
     */
    @PostMapping("/good/list/page/vo")
    @Cacheable(
            value = "good_app_page",
            key = "T(com.chao.aicode.util.CacheKeyUtils).generateKey(#appQueryRequest)",
            condition = "#appQueryRequest.pageNum <= 10"
    )
    public ApiResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, HTTPResponseCode.PARAM_ERROR);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, HTTPResponseCode.PARAM_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 分页查询
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ApiResponse.success(appVOPage);
    }

    /**
     * 根据 id 删除任意应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, HTTPResponseCode.PARAM_ERROR);
        Boolean result = appService.deleteApp(deleteRequest.getId());
        return ApiResponse.success(result);
    }

    /**
     * 根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null, HTTPResponseCode.PARAM_ERROR);
        ThrowUtils.throwIf(appAdminUpdateRequest.getId() == null, HTTPResponseCode.PARAM_ERROR);
        Boolean result = appService.updateAppByAdmin(appAdminUpdateRequest);
        return ApiResponse.success(result);
    }

    /**
     * 分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<AppVO>> listAppByPageForAdmin(@RequestBody AppQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, HTTPResponseCode.PARAM_ERROR);
        Page<AppVO> appVOPage = appService.listAppByPageForAdmin(queryRequest);
        return ApiResponse.success(appVOPage);
    }

    /**
     * 根据 id 查看应用详情
     */
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<AppVO> getAppById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, HTTPResponseCode.PARAM_ERROR);
        AppVO appVO = appService.getAppVOById(id);
        return ApiResponse.success(appVO);
    }
}
