package com.ruoyi.hys.controller.app;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hys.config.HysMemberContext;
import com.ruoyi.hys.domain.dto.HysAppDtos;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.service.HysMemberService;

/**
 * 小程序 · 账号接口。
 *
 * <p>
 * 登录 / 注册 / 找回密码免登录（由 {@code HysWebConfig} 排除拦截 + {@code @Anonymous} 放行安全链），
 * 修改密码与退出登录需要令牌。退出登录通过递增令牌版本实现<b>服务端失效</b>，
 * 而不是只让前端删本地缓存（后者在令牌被复制时毫无防护作用）。
 * </p>
 */
@Anonymous
@RestController
@RequestMapping("/app/auth")
public class HysAppAuthController extends BaseController {

    private final HysMemberService memberService;

    private final HysMemberMapper memberMapper;

    public HysAppAuthController(HysMemberService memberService, HysMemberMapper memberMapper) {
        this.memberService = memberService;
        this.memberMapper = memberMapper;
    }

    /**
     * 注册（推荐人手机号必填）。
     */
    @PostMapping("/register")
    public AjaxResult register(@Validated @RequestBody HysAppDtos.Register body) {
        HysMemberService.AuthResult result = memberService.register(body);
        AjaxResult ajax = AjaxResult.success("注册成功", result.member());
        ajax.put("token", result.token());
        return ajax;
    }

    /**
     * 登录。
     */
    @PostMapping("/login")
    public AjaxResult login(@Validated @RequestBody HysAppDtos.Login body) {
        HysMemberService.AuthResult result = memberService.login(body);
        AjaxResult ajax = AjaxResult.success("登录成功", result.member());
        ajax.put("token", result.token());
        return ajax;
    }

    /**
     * 退出登录：令牌版本 +1，当前及历史令牌全部失效。
     */
    @PostMapping("/logout")
    public AjaxResult logout() {
        Long memberId = HysMemberContext.requireMemberId();
        memberMapper.increaseTokenVersion(memberId);
        return success("已退出登录");
    }

    /**
     * 修改密码（成功后旧令牌失效，前端应跳转登录页）。
     */
    @PostMapping("/password")
    public AjaxResult changePassword(@Validated @RequestBody HysAppDtos.ChangePassword body) {
        memberService.changePassword(HysMemberContext.requireMemberId(), body);
        return success("密码修改成功，请重新登录");
    }

    /**
     * 找回密码。
     */
    @PostMapping("/forgot")
    public AjaxResult forgot(@Validated @RequestBody HysAppDtos.ForgotPassword body) {
        memberService.forgotPassword(body);
        return success("密码已重置，请使用新密码登录");
    }
}
