package com.chao.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.chao.aicode.ai.AiCodeGenTypeRoutingService;
import com.chao.aicode.ai.AiCodeGenTypeRoutingServiceFactory;
import com.chao.aicode.common.constants.AppConstant;
import com.chao.aicode.common.constants.UserConstant;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.converter.AppConverter;
import com.chao.aicode.core.AiCodeGeneratorFacade;
import com.chao.aicode.core.builder.VueProjectBuilder;
import com.chao.aicode.core.handler.StreamHandlerExecutor;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.exception.ThrowUtils;
import com.chao.aicode.mapper.AppMapper;
import com.chao.aicode.model.dto.app.AppAddRequest;
import com.chao.aicode.model.dto.app.AppAdminUpdateRequest;
import com.chao.aicode.model.dto.app.AppQueryRequest;
import com.chao.aicode.model.dto.app.AppUpdateRequest;
import com.chao.aicode.model.entity.App;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.model.enums.CodeGenTypeEnum;
import com.chao.aicode.model.vo.AppVO;
import com.chao.aicode.model.vo.UserVO;
import com.chao.aicode.service.AppService;
import com.chao.aicode.service.ChatHistoryService;
import com.chao.aicode.service.ScreenshotService;
import com.chao.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final UserService userService;
    private final AiCodeGeneratorFacade aiCodeGeneratorFacade;
    private final ChatHistoryService chatHistoryService;
    private final StreamHandlerExecutor streamHandlerExecutor;
    private final VueProjectBuilder vueProjectBuilder;
    private final ScreenshotService screenshotService;
    private final AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User user) {
        // 1.校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(message), HTTPResponseCode.PARAM_ERROR, "参数不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, HTTPResponseCode.PARAM_ERROR, "应用ID错误");
        // 2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null || app.getUserId() == null, HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        // 3.权限校验仅本人可以和自己的应用对话
        ThrowUtils.throwIf(!user.getId().equals(app.getUserId()), HTTPResponseCode.UNAUTHORIZED, "无权限操作");

        // 4.获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, HTTPResponseCode.PARAM_ERROR, "代码生成类型错误");
        // 5.调用AI生成代码
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 8. 收集 AI 响应的内容，并且在完成后保存记录到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, user, codeGenTypeEnum);
    }

    @Override
    public String deploy(Long appId, User user) {
        // 1.校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, HTTPResponseCode.PARAM_ERROR, "应用ID错误");
        ThrowUtils.throwIf(user == null, HTTPResponseCode.UNAUTHORIZED);
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        // 3.权限校验仅本人可以部署自己的应用
        ThrowUtils.throwIf(!user.getId().equals(app.getUserId()), HTTPResponseCode.UNAUTHORIZED, "无权限操作");
        // 4.检查是否已有 deployKey -> 如果没有 则生成 6为 deployKey(字母加+数字)
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(AppConstant.DEFAULT_DEPLOY_NAME_LENGTH);
            app.setDeployKey(deployKey);
        }
        // 5.获取代码生成类型 获取原始代码生成路径 应用访问目录
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6.检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), HTTPResponseCode.RESOURCE_NOT_FOUND, "代码生成路径不存在,请先生成代码");
        }
        // 7.构建项目
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, HTTPResponseCode.SYSTEM_ERROR, "Vue 项目构建失败，请重试");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), HTTPResponseCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 构建完成后，需要将构建后的文件复制到部署目录
            sourceDir = distDir;
        }
        // 8.复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(HTTPResponseCode.OPERATION_FAILED, "部署失败");
        }
        // 9.更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean b = this.updateById(updateApp);
        ThrowUtils.throwIf(!b, HTTPResponseCode.OPERATION_FAILED, "更新应用部署信息失败");
        // 9.返回可访问的->url地址
        String appDeplod = StrUtil.format("{}/{}", AppConstant.CODE_DEPLOY_HOST, deployKey);
        generateAppScreenshotAsync(appId, appDeplod);
        return appDeplod;
    }

    /**
     * 异步生成应用截图
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String url) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, HTTPResponseCode.PARAM_ERROR, "应用ID错误");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");

        // 3. 调用截图工具生成截图
        new Thread(() -> {
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(url);
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl); // 补充设置截图URL字段
            boolean b = this.updateById(updateApp);
            if (!b) {
                log.error("更新应用截图失败，应用ID: {}", appId);
            }
        }).start();
    }

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 1. 校验参数
        if (appAddRequest == null) throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "请求参数不能为空");

        String initPrompt = appAddRequest.getInitPrompt();
        if (StrUtil.isBlank(appAddRequest.getInitPrompt()))
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "initPrompt不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, HTTPResponseCode.OPERATION_FAILED, "创建应用失败");
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }

    @Override
    public Boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        // 1. 校验参数
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "请求参数不能为空");
        }
        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 3. 查询应用是否存在且属于当前用户
        App app = this.getById(appUpdateRequest.getId());
        if (app == null) {
            throw new BusinessException(HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        }
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(HTTPResponseCode.FORBIDDEN, "无权限操作");
        }
        // 4. 更新应用（只更新应用名称）时间
        if (StrUtil.isNotBlank(appUpdateRequest.getAppName())) {
            app.setAppName(appUpdateRequest.getAppName());
            app.setEditTime(LocalDateTime.now());
        }
        return this.updateById(app);
    }

    @Override
    public Boolean deleteMyApp(Long id, HttpServletRequest request) {
        // 1. 校验参数
        if (id == null || id <= 0) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "应用ID不能为空");
        }
        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 3. 查询应用是否存在且属于当前用户
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        }
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(HTTPResponseCode.FORBIDDEN, "无权限操作");
        }
        // 4. 删除应用
        return this.removeById(id);
    }

    @Override
    public AppVO getAppVOById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "应用ID不能为空");
        }
        log.info("查询应用详情：{}", id);
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        }
        return getAppVO(app);
    }

    @Override
    public Page<AppVO> listMyAppByPage(AppQueryRequest queryRequest, HttpServletRequest request) {
        // 1. 校验参数
        if (queryRequest == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "请求参数不能为空");
        }
        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 3. 设置用户ID过滤条件
        queryRequest.setUserId(loginUser.getId());
        // 4. 限制每页最多20个
        if (queryRequest.getPageSize() > 20) {
            queryRequest.setPageSize(20);
        }
        // 5. 查询
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        QueryWrapper queryWrapper = getQueryWrapper(queryRequest);
        return ToDo(pageNum, pageSize, queryWrapper);
    }

    @Override
    public Page<AppVO> listFeaturedAppByPage(AppQueryRequest queryRequest) {
        // 1. 校验参数
        if (queryRequest == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "请求参数不能为空");
        }
        // 2. 限制每页最多20个
        if (queryRequest.getPageSize() > 20) {
            queryRequest.setPageSize(20);
        }
        // 3. 设置精选应用条件(priority >= GOOD_APP_PRIORITY)
        queryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        // 4. 查询
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        QueryWrapper queryWrapper = getQueryWrapper(queryRequest);
        // 添加优先级条件：priority >= GOOD_APP_PRIORITY
        queryWrapper.ge(App::getPriority, AppConstant.GOOD_APP_PRIORITY);
        return ToDo(pageNum, pageSize, queryWrapper);
    }

    @Override
    public Boolean deleteApp(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "应用ID不能为空");
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        }
        return this.removeById(id);
    }

    @Override
    public Boolean updateAppByAdmin(AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "请求参数不能为空");
        }
        App app = this.getById(appAdminUpdateRequest.getId());
        if (app == null) {
            throw new BusinessException(HTTPResponseCode.RESOURCE_NOT_FOUND, "应用不存在");
        }
        // 更新应用名称、封面、优先级
        if (StrUtil.isNotBlank(appAdminUpdateRequest.getAppName())) {
            app.setAppName(appAdminUpdateRequest.getAppName());
        }
        if (StrUtil.isNotBlank(appAdminUpdateRequest.getCover())) {
            app.setCover(appAdminUpdateRequest.getCover());
        }
        if (appAdminUpdateRequest.getPriority() != null) {
            app.setPriority(appAdminUpdateRequest.getPriority());
        }
        return this.updateById(app);
    }

    @Override
    public Page<AppVO> listAppByPageForAdmin(AppQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "请求参数不能为空");
        }
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        QueryWrapper queryWrapper = getAdminQueryWrapper(queryRequest);
        return ToDo(pageNum, pageSize, queryWrapper);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = AppConverter.INSTANCE.VoTo(app);
        // 填充用户信息
        if (app.getUserId() != null) {
            User user = userService.getById(app.getUserId());
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                appVO.setUser(userVO);
            }
        }
        return appVO;
    }

    @Override
    public List<AppVO> listAppVO(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量查询用户信息
        Set<Long> userIds = appList.stream().map(App::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, userService::getUserVO));
        // 转换为VO并填充用户信息
        return appList.stream().map(app -> {
            AppVO appVO = AppConverter.INSTANCE.VoTo(app);
            if (app.getUserId() != null) {
                appVO.setUser(userVOMap.get(app.getUserId()));
            }
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest request) {
        if (request == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_INVALID, "请求参数为空");
        }
        Long id = request.getId();
        String appName = request.getAppName();
        Long userId = request.getUserId();
        Integer priority = request.getPriority();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create().eq(App::getId, id).eq(App::getUserId, userId).eq(App::getPriority, priority).like(App::getAppName, appName);
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy(App::getCreateTime, false);
        }
        return queryWrapper;
    }

    @Override
    public QueryWrapper getAdminQueryWrapper(AppQueryRequest request) {
        if (request == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_INVALID, "请求参数为空");
        }
        Long id = request.getId();
        String appName = request.getAppName();
        String cover = request.getCover();
        String initPrompt = request.getInitPrompt();
        String codeGenType = request.getCodeGenType();
        String deployKey = request.getDeployKey();
        Integer priority = request.getPriority();
        Long userId = request.getUserId();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create().eq(App::getId, id).eq(App::getUserId, userId).eq(App::getPriority, priority).like(App::getAppName, appName).like(App::getCover, cover).like(App::getInitPrompt, initPrompt).eq(App::getCodeGenType, codeGenType).eq(App::getDeployKey, deployKey);
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy(App::getCreateTime, false);
        }
        return queryWrapper;
    }

    /**
     * 通用分页查询方法
     *
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param queryWrapper 查询
     * @return 分页结果
     */
    private Page<AppVO> ToDo(long pageNum, long pageSize, QueryWrapper queryWrapper) {
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 转换为VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = listAppVO(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }


    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 删除应用时，关联删除对话历史
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            boolean b = chatHistoryService.deleteByAppId(appId);
            if (!b) {
                log.error("删除应用关联的对话历史失败");
            }
        } catch (Exception e) {
            log.error("删除应用关联的对话历史失败：{}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }
}
