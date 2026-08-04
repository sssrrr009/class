package com.classmanage.security;

import com.classmanage.common.BusinessException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录鉴权拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截 Controller 方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;

        boolean needLogin = hm.hasMethodAnnotation(RequireLogin.class)
                || hm.getBeanType().isAnnotationPresent(RequireLogin.class)
                || hm.hasMethodAnnotation(RequireRole.class)
                || hm.getBeanType().isAnnotationPresent(RequireRole.class);

        if (!needLogin) {
            return true;
        }

        // 解析 Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            throw new BusinessException(401, "登录凭证无效或已过期");
        }

        Long userId = Long.valueOf(claims.getSubject());
        Role role = Role.valueOf(String.valueOf(claims.get("role")));
        UserContext.set(new LoginUser(userId, role));

        // 角色校验
        if (hm.hasMethodAnnotation(RequireRole.class) || hm.getBeanType().isAnnotationPresent(RequireRole.class)) {
            RequireRole rr = hm.hasMethodAnnotation(RequireRole.class)
                    ? hm.getMethodAnnotation(RequireRole.class)
                    : hm.getBeanType().getAnnotation(RequireRole.class);
            boolean allowed = false;
            for (Role r : rr.value()) {
                if (r == role) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new BusinessException(403, "无权访问该资源");
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
