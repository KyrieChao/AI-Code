package com.chao.aicode.aop;

import com.chao.aicode.annotation.AuthCheck;
import com.chao.aicode.common.response.HTTPResponseCode;
import com.chao.aicode.exception.BusinessException;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.model.enums.UserRoleEnum;
import com.chao.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        // 获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User user = userService.getLoginUser(request);
        UserRoleEnum role = UserRoleEnum.getEnumByValue(mustRole);
        // 放行
        if (role == null) {
            return joinPoint.proceed();
        }
        // 以下代码
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        //
        if (userRoleEnum == null) {
            throw new BusinessException(HTTPResponseCode.PERMISSION_DENIED);
        }
        // 要求必须有管理权限但是当前用户没有
        if (UserRoleEnum.ADMIN.equals(role) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(HTTPResponseCode.PERMISSION_DENIED);
        }
        // 通过普通用户的权限 放行
        return joinPoint.proceed();
    }
}
