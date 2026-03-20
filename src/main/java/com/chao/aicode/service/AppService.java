package com.chao.aicode.service;

import com.chao.aicode.model.dto.app.AppAddRequest;
import com.chao.aicode.model.dto.app.AppQueryRequest;
import com.chao.aicode.model.dto.app.AppUpdateRequest;
import com.chao.aicode.model.entity.App;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
public interface AppService extends IService<App> {

    /**
     * 通过对话生成代码
     */
    Flux<String> chatToGenCode(Long userId, String message, User user);

    /**
     * 部署应用
     */
    String deploy(Long appId, User user);

    void generateAppScreenshotAsync(Long appId, String url);

    Long createApp(AppAddRequest appAddRequest, User loginUser);


    /**
     * 用户更新自己的应用
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
     */
    AppVO getAppVOById(Long id);

    /**
     * 分页查询用户自己的应用列表
     */
    Page<AppVO> listMyAppByPage(AppQueryRequest queryRequest, HttpServletRequest request);

    /**
     * 分页查询精选应用列表
     */
    Page<AppVO> listFeaturedAppByPage(AppQueryRequest queryRequest);

    /**
     * 管理员删除任意应用
     */
    Boolean deleteApp(Long id);

    /**
     * 管理员更新任意应用
     */
    Boolean updateAppByAdmin(com.chao.aicode.model.dto.app.AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页查询应用列表
     */
    Page<AppVO> listAppByPageForAdmin(AppQueryRequest queryRequest);

    /**
     * 获取应用VO
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用VO列表
     */
    List<AppVO> listAppVO(List<App> appList);

    /**
     * 根据查询条件构造数据查询参数（用户查询）
     */
    QueryWrapper getQueryWrapper(AppQueryRequest request);

    /**
     * 根据查询条件构造数据查询参数（管理员查询）
     */
    QueryWrapper getAdminQueryWrapper(AppQueryRequest request);


    /**
     * 获取应用封装类列表
     */
    List<AppVO> getAppVOList(List<App> appList);

}