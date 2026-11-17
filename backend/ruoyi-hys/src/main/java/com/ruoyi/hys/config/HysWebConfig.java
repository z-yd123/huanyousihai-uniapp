package com.ruoyi.hys.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 小程序接口拦截器注册。
 *
 * <p>
 * 仅拦截 /app/**：登录、注册、找回密码、公开配置这四类无需登录，其余一律要求会员令牌。
 * 后台 /hys/** 不在此处拦截，交由 Spring Security + 菜单权限控制。
 * </p>
 */
@Configuration
public class HysWebConfig implements WebMvcConfigurer {

    private final HysAuthInterceptor authInterceptor;

    public HysWebConfig(HysAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/app/**")
                .excludePathPatterns(
                        "/app/auth/login",
                        "/app/auth/register",
                        "/app/auth/forgot",
                        "/app/public/**");
    }
}
