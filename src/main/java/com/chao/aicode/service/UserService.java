package com.chao.aicode.service;

import com.chao.aicode.model.dto.user.UserQueryRequest;
import com.chao.aicode.model.vo.LoginUserVO;
import com.chao.aicode.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.chao.aicode.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 登录用户
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取加密后的密码
     */
    String getEncryptPassword(String password);

    /**
     * 获取脱敏的已登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息(分页)
     *
     * @param userList 用户列表
     * @return 脱敏
     */
    List<UserVO> listUserVO(List<User> userList);

    /**
     * 根据查询条件构造数据查询参数
     */
    QueryWrapper getQueryWrapper(UserQueryRequest request);

    Boolean usrLogout(HttpServletRequest request);
}
