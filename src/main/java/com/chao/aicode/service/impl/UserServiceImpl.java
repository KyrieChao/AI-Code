package com.chao.aicode.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.chao.aicode.common.constants.UserConstant;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.mapper.UserMapper;
import com.chao.aicode.converter.UserConverter;
import com.chao.aicode.model.dto.user.UserQueryRequest;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.model.enums.UserRoleEnum;
import com.chao.aicode.model.vo.LoginUserVO;
import com.chao.aicode.model.vo.UserVO;
import com.chao.aicode.service.UserService;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/kyriechao">陈鸽涛</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BaseMapper baseMapper;

    public UserServiceImpl(BaseMapper baseMapper) {
        this.baseMapper = baseMapper;
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "参数不能为空");
        }
        if (userPassword.length() <= 8 || checkPassword.length() <= 8) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "密码长度不能小于8位");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "两次密码不一致");
        }
        // 2.查询用户是否存在
        long count = this.mapper.selectCountByQuery(new QueryWrapper().eq(User::getUserAccount, userAccount));
        if (count > 0) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "用户已存在");
        }
        // 3.加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 4.创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户" + System.currentTimeMillis());
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean b = this.save(user);
        if (!b) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "参数不能为空");
        }
        if (userPassword.length() <= 8) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "密码长度不能小于8位");
        }
        // 2.加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3.查询用户
        User user = this.mapper.selectOneByQuery(new QueryWrapper()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword)
        );
        if (user == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_ERROR, "用户不存在或密码错误");
        }
        // 4.记录用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 5.返回脱敏用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null || user.getId() == null) {
            throw new BusinessException(HTTPResponseCode.UNAUTHORIZED);
        }
        // 查询用户 获取罪行信息
        user = this.mapper.selectOneById(user.getId());
        if (user == null) {
            throw new BusinessException(HTTPResponseCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    public String getEncryptPassword(String password) {
        // 盐值 混淆密码
        final String SALT = "ChaoAICode";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) return null;
        return UserConverter.INSTANCE.LoginVoTo(user);
    }

    @Override
    public List<UserVO> listUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) return new ArrayList<>();
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        return UserConverter.INSTANCE.VoTo(user);
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(HTTPResponseCode.PARAM_INVALID, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq(User::getId, id)
                .eq(User::getUserRole, userRole)
                .like(User::getUserAccount, userAccount)
                .like(User::getUserName, userName)
                .like(User::getUserProfile, userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Boolean usrLogout(HttpServletRequest request) {
        Object o = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (o == null) return false;
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }
}
