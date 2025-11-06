package com.chao.aicode.controller;

import com.chao.aicode.annotation.AuthCheck;
import com.chao.aicode.common.constants.UserConstant;
import com.chao.aicode.common.request.DeleteRequest;
import com.chao.aicode.common.response.ApiResponse;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.exception.ThrowUtils;
import com.chao.aicode.converter.UserConverter;
import com.chao.aicode.model.dto.user.*;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.model.vo.LoginUserVO;
import com.chao.aicode.model.vo.UserVO;
import com.chao.aicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<Long> register(@RequestBody UserRegisterRequest request) {
        long l = userService.userRegister(request.getUserAccount(), request.getUserPassword(), request.getCheckPassword());
        return ApiResponse.success(l);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@RequestBody UserLoginRequest user, HttpServletRequest request) {
        ThrowUtils.throwIf(user == null, HTTPResponseCode.PARAM_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(user.getUserAccount(), user.getUserPassword(), request);
        return ApiResponse.success(loginUserVO);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public ApiResponse<LoginUserVO> info(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        LoginUserVO vo = userService.getLoginUserVO(user);
        return ApiResponse.success(vo);

    }

    /**
     * 用户登出
     */
    @GetMapping("/logout")
    public ApiResponse<Boolean> logout(HttpServletRequest request) {
        ThrowUtils.throwIf(userService.getLoginUser(request) == null, HTTPResponseCode.USER_NOT_FOUND);
        return ApiResponse.success(userService.usrLogout(request));
    }


    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, HTTPResponseCode.PARAM_ERROR);
        User user = UserConverter.INSTANCE.ToAddRequest(userAddRequest);
        // 默认密码 12345678
        String encryptPassword = userService.getEncryptPassword(UserConstant.DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, HTTPResponseCode.USER_ERROR);
        return ApiResponse.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, HTTPResponseCode.PARAM_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, HTTPResponseCode.USER_NOT_FOUND);
        return ApiResponse.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public ApiResponse<UserVO> getUserVOById(long id) {
        ApiResponse<User> response = getUserById(id);
        User user = response.getData();
        return ApiResponse.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ApiResponse.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR);
        }
        User user = UserConverter.INSTANCE.ToUpdateRequest(userUpdateRequest);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, HTTPResponseCode.USER_ERROR);
        return ApiResponse.success();
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param query 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest query) {
        ThrowUtils.throwIf(query == null, HTTPResponseCode.PARAM_ERROR);
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), userService.getQueryWrapper(query));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.listUserVO(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ApiResponse.success(userVOPage);
    }

}
