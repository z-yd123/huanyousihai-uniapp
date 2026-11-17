package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.dto.HysAdminDtos;
import com.ruoyi.hys.service.HysCommissionService;
import com.ruoyi.hys.service.HysMemberService;

/**
 * 管理端 · 会员管理（含推荐关系异常补绑与佣金重算）。
 */
@RestController
@RequestMapping("/hys/member")
public class HysAdminMemberController extends BaseController {

    private final HysMemberService memberService;

    private final HysCommissionService commissionService;

    public HysAdminMemberController(HysMemberService memberService, HysCommissionService commissionService) {
        this.memberService = memberService;
        this.commissionService = commissionService;
    }

    @PreAuthorize("@ss.hasPermi('hys:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysMember query) {
        startPage();
        List<HysMember> list = memberService.adminList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:member:list')")
    @GetMapping("/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId) {
        return success(memberService.adminGet(memberId));
    }

    /**
     * 资料维护：只允许改名称 / 区域 / 状态，余额与推荐关系不提供直接编辑入口（防越权改钱）。
     */
    @PreAuthorize("@ss.hasPermi('hys:member:edit')")
    @Log(title = "环游四海会员", businessType = BusinessType.UPDATE)
    @PutMapping("/{memberId}")
    public AjaxResult editMember(@PathVariable Long memberId,
            @Validated @RequestBody HysAdminDtos.MemberEdit body) {
        memberService.adminUpdate(memberId, body);
        return success("已保存");
    }

    /**
     * 补绑推荐人（仅异常账号），成功后自动重算该会员历史未发放佣金。
     */
    @PreAuthorize("@ss.hasPermi('hys:member:rebind')")
    @Log(title = "环游四海会员补绑", businessType = BusinessType.UPDATE)
    @PostMapping("/{memberId}/rebind")
    public AjaxResult rebind(@PathVariable Long memberId, @Validated @RequestBody HysAdminDtos.Rebind body) {
        memberService.rebindReferrer(memberId, body);
        int fixed = commissionService.recomputeByMember(memberId);
        return success("补绑成功，已重算 " + fixed + " 笔历史订单佣金");
    }

    /**
     * 重置会员登录密码（重置后该会员所有令牌立即失效）。
     */
    @PreAuthorize("@ss.hasPermi('hys:member:edit')")
    @Log(title = "环游四海会员", businessType = BusinessType.UPDATE)
    @PostMapping("/{memberId}/resetPwd")
    public AjaxResult resetPwd(@PathVariable Long memberId, @RequestBody java.util.Map<String, String> body) {
        memberService.adminResetPassword(memberId, body.get("password"));
        return success("密码已重置");
    }
}
