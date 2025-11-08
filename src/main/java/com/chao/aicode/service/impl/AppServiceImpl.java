package com.chao.aicode.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.chao.aicode.common.constants.AppConstant;
import com.chao.aicode.common.constants.UserConstant;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.mapper.AppMapper;
import com.chao.aicode.converter.AppConverter;
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
import com.chao.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final UserService userService;

    public AppServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Long addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        // 1. 校验参数
        if (appAddRequest == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "请求参数不能为空");
        }
        if (StrUtil.isBlank(appAddRequest.getInitPrompt())) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "initPrompt不能为空");
        }
        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 3. 创建应用
        App app = AppConverter.INSTANCE.ToAddRequest(appAddRequest);
        app.setUserId(loginUser.getId());
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        // 暂定为 initPrompt 前12个字
        if (StrUtil.isNotBlank(app.getAppName())) {
            app.setAppName(app.getInitPrompt().substring(0, Math.min(app.getInitPrompt().length(), 12)));
        } else {
            app.setAppName(app.getAppName());
        }
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        boolean result = this.save(app);
        if (!result) {
            throw new BusinessException(HTTPResponseCode.OPERATION_FAILED, "创建应用失败");
        }
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
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
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
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(App::getId, id)
                .eq(App::getUserId, userId)
                .eq(App::getPriority, priority)
                .like(App::getAppName, appName);
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
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(App::getId, id)
                .eq(App::getUserId, userId)
                .eq(App::getPriority, priority)
                .like(App::getAppName, appName)
                .like(App::getCover, cover)
                .like(App::getInitPrompt, initPrompt)
                .eq(App::getCodeGenType, codeGenType)
                .eq(App::getDeployKey, deployKey);
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
}
