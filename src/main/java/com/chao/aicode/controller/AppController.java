package com.chao.aicode.controller;

import cn.hutool.core.util.StrUtil;
import com.chao.aicode.annotation.AuthCheck;
import com.chao.aicode.common.constants.UserConstant;
import com.chao.aicode.common.request.DeleteRequest;
import com.chao.aicode.common.response.ApiResponse;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.ThrowUtils;
import com.chao.aicode.model.dto.app.AppAddRequest;
import com.chao.aicode.model.dto.app.AppAdminUpdateRequest;
import com.chao.aicode.model.dto.app.AppQueryRequest;
import com.chao.aicode.model.dto.app.AppUpdateRequest;
import com.chao.aicode.model.vo.AppVO;
import com.chao.aicode.service.AppService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 创建应用
     */
    @PostMapping("/add")
    public ApiResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, HTTPResponseCode.PARAM_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(appAddRequest.getInitPrompt()), HTTPResponseCode.PARAM_ERROR, "initPrompt不能为空");
        Long appId = appService.addApp(appAddRequest, request);
        return ApiResponse.success(appId);
    }

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     */
    @PostMapping("/update/my")
    public ApiResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null, HTTPResponseCode.PARAM_ERROR);
        ThrowUtils.throwIf(appUpdateRequest.getId() == null, HTTPResponseCode.PARAM_ERROR);
        Boolean result = appService.updateMyApp(appUpdateRequest, request);
        return ApiResponse.success(result);
    }

    /**
     * 根据 id 删除自己的应用
     */
    @PostMapping("/delete/my")
    public ApiResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, HTTPResponseCode.PARAM_ERROR);
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
    public ApiResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest queryRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, HTTPResponseCode.PARAM_ERROR);
        Page<AppVO> appVOPage = appService.listMyAppByPage(queryRequest, request);
        return ApiResponse.success(appVOPage);
    }

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多20个）
     */
    @PostMapping("/list/featured/page/vo")
    public ApiResponse<Page<AppVO>> listFeaturedAppByPage(@RequestBody AppQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, HTTPResponseCode.PARAM_ERROR);
        Page<AppVO> appVOPage = appService.listFeaturedAppByPage(queryRequest);
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
