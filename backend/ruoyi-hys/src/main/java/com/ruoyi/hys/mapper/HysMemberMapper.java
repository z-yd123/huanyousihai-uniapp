package com.ruoyi.hys.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hys.domain.HysMember;

/**
 * 会员数据访问。所有金额变动都走「条件更新 + 影响行数校验」，不做「先查后改」，
 * 避免并发下余额被覆盖（单边账）。
 */
public interface HysMemberMapper {

    HysMember selectMemberById(Long memberId);

    HysMember selectMemberByPhone(String phone);

    List<HysMember> selectMemberList(HysMember query);

    int insertMember(HysMember member);

    int updateMember(HysMember member);

    int updateLoginInfo(HysMember member);

    /** 令牌版本 +1：改密 / 停用后旧令牌立即失效 */
    int increaseTokenVersion(Long memberId);

    int updateVip(@Param("memberId") Long memberId, @Param("level") String level,
            @Param("vipExpireAt") LocalDateTime vipExpireAt);

    /** 写入 / 修正推荐关系（注册补路径、后台补绑、异常账号修复共用） */
    int updateReferrer(@Param("memberId") Long memberId, @Param("referrerId") Long referrerId,
            @Param("referrerPath") String referrerPath, @Param("directSeq") Integer directSeq);

    /** 后台重置密码 */
    int resetPassword(@Param("memberId") Long memberId, @Param("password") String password);

    /**
     * 余额增减（delta 可正可负），负数时带余额充足校验。
     *
     * @return 影响行数，0 表示余额不足或会员不存在
     */
    int changeBalance(@Param("memberId") Long memberId, @Param("delta") long delta);

    /**
     * 允许扣成负数的余额变动，仅用于售后退款回冲佣金（受益人可能已把钱花掉）。
     * 正常业务禁止调用。
     */
    int changeBalanceAny(@Param("memberId") Long memberId, @Param("delta") long delta);

    /** 可用余额 → 冻结余额（提现申请） */
    int freezeBalance(@Param("memberId") Long memberId, @Param("amount") long amount);

    /** 冻结余额 → 可用余额（提现驳回） */
    int unfreezeBalance(@Param("memberId") Long memberId, @Param("amount") long amount);

    /** 扣减冻结余额（提现通过，资金离场） */
    int releaseFrozen(@Param("memberId") Long memberId, @Param("amount") long amount);

    long countDirect(Long referrerId);

    long countTeam(@Param("path") String path, @Param("excludeId") Long excludeId);

    long countNewSince(@Param("path") String path, @Param("excludeId") Long excludeId, @Param("since") LocalDateTime since);

    List<HysMember> selectDirects(Long referrerId);

    long countAll();

    long countVip(@Param("now") LocalDateTime now);

    long countAbnormal();
}
