package com.chao.aicode.service;

import com.chao.aicode.model.dto.app.AppAddRequest;
import com.chao.aicode.model.dto.app.AppQueryRequest;
import com.chao.aicode.model.dto.app.AppUpdateRequest;
import com.chao.aicode.model.entity.App;
import com.chao.aicode.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appAddRequest 应用创建请求
     * @param request       HTTP请求
     * @return 应用ID
     */
    Long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 用户更新自己的应用
     *
     * @param appUpdateRequest 应用更新请求
     * @param request          HTTP请求
     * @return 是否成功
     */
    Boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 用户删除自己的应用
     *
     * @param id      应用ID
     * @param request HTTP请求
     * @return 是否成功
     */
    Boolean deleteMyApp(Long id, HttpServletRequest request);

    /**
     * 根据ID获取应用详情
     *
     * @param id 应用ID
     * @return 应用详情
     */
    AppVO getAppVOById(Long id);

    /**
     * 分页查询用户自己的应用列表
     *
     * @param queryRequest 查询请求
     * @param request      HTTP请求
     * @return 分页结果
     */
    Page<AppVO> listMyAppByPage(AppQueryRequest queryRequest, HttpServletRequest request);

    /**
     * 分页查询精选应用列表
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    Page<AppVO> listFeaturedAppByPage(AppQueryRequest queryRequest);

    /**
     * 管理员删除任意应用
     *
     * @param id 应用ID
     * @return 是否成功
     */
    Boolean deleteApp(Long id);

    /**
     * 管理员更新任意应用
     *
     * @param appAdminUpdateRequest 管理员更新请求
     * @return 是否成功
     */
    Boolean updateAppByAdmin(com.chao.aicode.model.dto.app.AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页查询应用列表
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    Page<AppVO> listAppByPageForAdmin(AppQueryRequest queryRequest);

    /**
     * 获取应用VO
     *
     * @param app 应用实体
     * @return 应用VO
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用VO列表
     *
     * @param appList 应用实体列表
     * @return 应用VO列表
     */
    List<AppVO> listAppVO(List<App> appList);

    /**
     * 根据查询条件构造数据查询参数（用户查询）
     *
     * @param request 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest request);

    /**
     * 根据查询条件构造数据查询参数（管理员查询）
     *
     * @param request 查询请求
     * @return 查询包装器
     */
    QueryWrapper getAdminQueryWrapper(AppQueryRequest request);
}