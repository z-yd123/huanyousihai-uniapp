package com.ruoyi.hys.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.config.HysJwtService;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.dto.HysAdminDtos;
import com.ruoyi.hys.domain.dto.HysAppDtos;
import com.ruoyi.hys.domain.vo.HysVos;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.util.HysMasks;

/**
 * 会员服务：注册 / 登录 / 改密 / 找回密码 / 我的分享 / 后台维护。
 *
 * <p>
 * 关键业务约束（AGENT.md §4.1、§9.1 Q-11）：
 * </p>
 * <ul>
 * <li><b>无推荐人不得注册</b>——推荐关系是两级佣金链路的前置条件，故注册入口强制校验推荐人手机号；</li>
 * <li>推荐路径 {@code referrerPath} 形如 {@code ,1,2,3,}，注册时一次性写入，团队人数统计无需递归查询；</li>
 * <li>{@code directSeq} 记录「我是推荐人的第几个直推」，注册时固化，佣金分档只认它，避免事后重算口径漂移。</li>
 * </ul>
 */
@Service
public class HysMemberService {

    private static final Logger log = LoggerFactory.getLogger(HysMemberService.class);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HysMemberMapper memberMapper;

    private final HysJwtService jwtService;

    private final HysConfigService configService;

    public HysMemberService(HysMemberMapper memberMapper, HysJwtService jwtService, HysConfigService configService) {
        this.memberMapper = memberMapper;
        this.jwtService = jwtService;
        this.configService = configService;
    }

    /**
     * 登录结果。
     *
     * @param token  会员令牌
     * @param member 会员信息视图
     */
    public record AuthResult(String token, HysVos.Member member) {
    }

    // ------------------------------------------------------------------
    // 注册 / 登录 / 密码
    // ------------------------------------------------------------------

    /**
     * 注册。推荐人手机号必填，缺失直接拒绝（不允许建档）。
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthResult register(HysAppDtos.Register body) {
        String referrerCode = StringUtils.trim(body.referrerCode());
        HysMember referrer = memberMapper.selectMemberByPhone(referrerCode);
        if (referrer == null) {
            throw new ServiceException("推荐人不存在，请核对推荐人手机号");
        }
        if (HysConstants.MEMBER_DISABLED.equals(referrer.getStatus())) {
            throw new ServiceException("推荐人账号已停用，无法完成注册");
        }
        if (memberMapper.selectMemberByPhone(body.phone()) != null) {
            throw new ServiceException("该手机号已注册，请直接登录");
        }

        HysMember member = new HysMember();
        member.setPhone(body.phone());
        member.setNickname(StringUtils.trim(body.nickname()));
        member.setPassword(SecurityUtils.encryptPassword(body.password()));
        member.setProvince(body.province());
        member.setCity(body.city());
        member.setDistrict(body.district());
        member.setReferrerId(referrer.getMemberId());
        member.setReferrerPath("");
        member.setDirectSeq((int) memberMapper.countDirect(referrer.getMemberId()) + 1);
        member.setLevel(HysConstants.LEVEL_NORMAL);
        member.setBalance(0L);
        member.setFrozen(0L);
        member.setGreenPoints(0);
        member.setStatus(HysConstants.MEMBER_NORMAL);
        member.setRemark("注册绑定推荐人 " + HysMasks.phone(referrer.getPhone()));

        memberMapper.insertMember(member);

        // 推荐路径依赖自增主键，注册后补写一次：父路径 + 自己的ID + ','
        String parentPath = referrer.getReferrerPath() == null ? "," : referrer.getReferrerPath();
        String path = parentPath + member.getMemberId() + ",";
        memberMapper.updateReferrer(member.getMemberId(), referrer.getMemberId(), path, member.getDirectSeq());
        member.setReferrerPath(path);

        log.info("会员注册成功 memberId={} 推荐人={}", member.getMemberId(), HysMasks.phone(referrer.getPhone()));
        return new AuthResult(issueToken(member), toView(member));
    }

    /**
     * 登录。
     */
    public AuthResult login(HysAppDtos.Login body) {
        HysMember member = memberMapper.selectMemberByPhone(body.phone());
        // 账号不存在与密码错误返回同一提示，避免撞库探测
        if (member == null || !SecurityUtils.matchesPassword(body.password(), member.getPassword())) {
            throw new ServiceException("手机号或密码错误", HttpStatus.BAD_REQUEST);
        }
        if (HysConstants.MEMBER_DISABLED.equals(member.getStatus())) {
            throw new ServiceException("账号已停用，请联系客服", HttpStatus.FORBIDDEN);
        }
        memberMapper.updateLoginInfo(member);
        return new AuthResult(issueToken(member), toView(member));
    }

    /**
     * 修改密码：成功后递增令牌版本，旧令牌全部失效，前端需重新登录。
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long memberId, HysAppDtos.ChangePassword body) {
        HysMember member = requireMember(memberId);
        if (!SecurityUtils.matchesPassword(body.oldPassword(), member.getPassword())) {
            throw new ServiceException("原密码不正确");
        }
        if (body.oldPassword().equals(body.newPassword())) {
            throw new ServiceException("新密码不能与原密码相同");
        }
        memberMapper.resetPassword(memberId, SecurityUtils.encryptPassword(body.newPassword()));
    }

    /**
     * 找回密码（未登录）：手机号 + 注册名称双因子。1.0 未接短信，故用名称做弱校验并在文档中标注（Q-06）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(HysAppDtos.ForgotPassword body) {
        HysMember member = memberMapper.selectMemberByPhone(body.phone());
        if (member == null || !member.getNickname().equals(StringUtils.trim(body.nickname()))) {
            throw new ServiceException("手机号与名称不匹配，请核对后重试");
        }
        memberMapper.resetPassword(member.getMemberId(), SecurityUtils.encryptPassword(body.newPassword()));
        log.info("会员 {} 通过找回密码重置了登录密码", member.getMemberId());
    }

    // ------------------------------------------------------------------
    // 个人中心 / 我的分享
    // ------------------------------------------------------------------

    public HysMember requireMember(Long memberId) {
        HysMember member = memberMapper.selectMemberById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在", HttpStatus.UNAUTHORIZED);
        }
        return member;
    }

    public HysVos.Member profile(Long memberId) {
        return toView(requireMember(memberId));
    }

    /**
     * 我的分享 · 概览：推荐人、团队人数（全部后代）、个人人数（直推）与本月新增。
     */
    public HysVos.ShareOverview shareOverview(Long memberId) {
        HysMember member = requireMember(memberId);
        HysMember referrer = member.getReferrerId() == null ? null
                : memberMapper.selectMemberById(member.getReferrerId());
        String path = memberPath(member);
        long teamCount = memberMapper.countTeam(path, memberId);
        long directCount = memberMapper.countDirect(memberId);
        long monthNew = memberMapper.countNewSince(path, memberId, LocalDateTime.now().withDayOfMonth(1).toLocalDate()
                .atStartOfDay());
        return new HysVos.ShareOverview(
                referrer == null ? "—" : referrer.getNickname(),
                referrer == null ? "—" : HysMasks.phone(referrer.getPhone()),
                teamCount, directCount, monthNew, member.getPhone(), configService.appNotice());
    }

    /**
     * 我的分享 · 直推名单（手机号脱敏、团队人数、个人人数、会员性质、注册时间）。
     */
    public List<HysVos.DirectItem> directList(Long memberId) {
        return memberMapper.selectDirects(memberId).stream()
                .map(item -> new HysVos.DirectItem(
                        item.getMemberId(),
                        item.getNickname(),
                        HysMasks.phone(item.getPhone()),
                        item.getLevel(),
                        isVipActive(item),
                        item.getTeamCount() == null ? 0L : item.getTeamCount(),
                        item.getDirectCount() == null ? 0L : item.getDirectCount(),
                        item.getCreateTime() == null ? "" : item.getCreateTime().format(TIME_FMT)))
                .toList();
    }

    // ------------------------------------------------------------------
    // 后台维护
    // ------------------------------------------------------------------

    public List<HysMember> adminList(HysMember query) {
        List<HysMember> list = memberMapper.selectMemberList(query);
        // 列表统一脱敏，避免运营截图外泄完整手机号
        list.forEach(item -> {
            item.setReferrerPhone(item.getReferrerPhone() == null ? null : HysMasks.phone(item.getReferrerPhone()));
        });
        return list;
    }

    public HysMember adminGet(Long memberId) {
        return memberMapper.selectMemberById(memberId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(Long memberId, HysAdminDtos.MemberEdit body) {
        HysMember member = requireMember(memberId);
        member.setNickname(StringUtils.trim(body.nickname()));
        member.setProvince(body.province());
        member.setCity(body.city());
        member.setDistrict(body.district());
        member.setStatus(body.status());
        member.setRemark(body.remark() == null ? member.getRemark() : body.remark());
        memberMapper.updateMember(member);
        if (HysConstants.MEMBER_DISABLED.equals(body.status())) {
            // 停用立即踢下线
            memberMapper.increaseTokenVersion(memberId);
        }
    }

    /**
     * 补绑推荐人（仅异常账号）。补绑后递增令牌版本并将状态恢复为正常，历史佣金由
     * {@code HysCommissionService#recomputeByMember} 重算。
     */
    @Transactional(rollbackFor = Exception.class)
    public void rebindReferrer(Long memberId, HysAdminDtos.Rebind body) {
        HysMember member = requireMember(memberId);
        if (member.getReferrerId() != null && HysConstants.MEMBER_NORMAL.equals(member.getStatus())) {
            throw new ServiceException("该会员推荐关系正常，不允许改绑（推荐关系一经建立不可修改）");
        }
        HysMember referrer = memberMapper.selectMemberByPhone(body.referrerPhone());
        if (referrer == null) {
            throw new ServiceException("推荐人不存在");
        }
        if (referrer.getMemberId().equals(memberId)) {
            throw new ServiceException("不能把自己设为推荐人");
        }
        if (memberPath(referrer).contains("," + memberId + ",")) {
            throw new ServiceException("不能绑定自己的下级为推荐人（会形成环）");
        }
        int seq = (int) memberMapper.countDirect(referrer.getMemberId()) + 1;
        String path = memberPath(referrer) + memberId + ",";
        memberMapper.updateReferrer(memberId, referrer.getMemberId(), path, seq);
        HysMember patch = new HysMember();
        patch.setMemberId(memberId);
        patch.setStatus(HysConstants.MEMBER_NORMAL);
        patch.setRemark("补绑推荐人：" + HysMasks.phone(referrer.getPhone()) + "；" + (body.remark() == null ? "" : body.remark()));
        memberMapper.updateMember(patch);
        log.warn("异常账号补绑推荐人 memberId={} 新推荐人={}", memberId, HysMasks.phone(referrer.getPhone()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminResetPassword(Long memberId, String newPassword) {
        requireMember(memberId);
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new ServiceException("新密码长度需为 6-20 位");
        }
        memberMapper.resetPassword(memberId, SecurityUtils.encryptPassword(newPassword));
    }

    /**
     * 升级 / 续费 VIP：在原到期时间（未过期时）基础上顺延，避免续费损失剩余天数。
     */
    @Transactional(rollbackFor = Exception.class)
    public LocalDateTime activateVip(Long memberId) {
        HysMember member = requireMember(memberId);
        int years = Math.max(1, configService.getInt(HysConstants.CFG_VIP_RENEW_YEARS, 1));
        LocalDateTime base = isVipActive(member) ? member.getVipExpireAt() : LocalDateTime.now();
        LocalDateTime expireAt = base.plusYears(years);
        memberMapper.updateVip(memberId, HysConstants.LEVEL_VIP, expireAt);
        return expireAt;
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /** 会员自身路径（含自己），用于团队统计 */
    private String memberPath(HysMember member) {
        if (StringUtils.isNotEmpty(member.getReferrerPath())) {
            return member.getReferrerPath();
        }
        return (member.getReferrerId() == null ? "," : ",") + member.getMemberId() + ",";
    }

    private boolean isVipActive(HysMember member) {
        return HysConstants.LEVEL_VIP.equals(member.getLevel()) && member.getVipExpireAt() != null
                && member.getVipExpireAt().isAfter(LocalDateTime.now());
    }

    private String issueToken(HysMember member) {
        int version = member.getTokenVersion() == null ? 0 : member.getTokenVersion();
        return jwtService.createToken(member.getMemberId(), version);
    }

    /**
     * 实体 → 视图：只暴露前端需要的字段，手机号脱敏（本人手机号除外，本人需要看到自己的邀请码）。
     */
    public HysVos.Member toView(HysMember member) {
        String region = String.join("", nullToEmpty(member.getProvince()), nullToEmpty(member.getCity()),
                nullToEmpty(member.getDistrict()));
        // 推荐人信息按需回查（注册 / 登录 / 个人中心都需要展示“我的推荐人”）
        HysMember referrer = member.getReferrerId() == null ? null
                : memberMapper.selectMemberById(member.getReferrerId());
        return new HysVos.Member(
                member.getMemberId(),
                member.getPhone(),
                member.getNickname(),
                member.getProvince(),
                member.getCity(),
                member.getDistrict(),
                region,
                member.getLevel(),
                isVipActive(member),
                member.getVipExpireAt() == null ? null : member.getVipExpireAt().format(TIME_FMT),
                member.getBalance() == null ? 0L : member.getBalance(),
                member.getFrozen() == null ? 0L : member.getFrozen(),
                member.getGreenPoints() == null ? 0 : member.getGreenPoints(),
                referrer == null ? "平台运营" : referrer.getNickname(),
                referrer == null ? "—" : HysMasks.phone(referrer.getPhone()),
                member.getPhone(),
                member.getCreateTime() == null ? null : member.getCreateTime().format(TIME_FMT));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
