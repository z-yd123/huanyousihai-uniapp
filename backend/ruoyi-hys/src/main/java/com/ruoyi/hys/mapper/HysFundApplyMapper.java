package com.ruoyi.hys.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hys.domain.HysFundApply;

/**
 * 充值 / 提现申请数据访问。
 */
public interface HysFundApplyMapper {

    int insertApply(HysFundApply apply);

    HysFundApply selectApplyById(Long applyId);

    List<HysFundApply> selectApplyList(HysFundApply query);

    /**
     * 审核：仅当当前状态为待审核时生效（防止重复审核 / 并发审核造成重复入账）。
     *
     * @return 影响行数，0 表示已被处理
     */
    int auditApply(@Param("applyId") Long applyId, @Param("status") Integer status,
            @Param("auditBy") String auditBy, @Param("auditRemark") String auditRemark,
            @Param("actualAmount") Long actualAmount);

    long countPending();

    long sumPassedByMember(@Param("memberId") Long memberId, @Param("applyType") String applyType);

    long countPendingByMember(@Param("memberId") Long memberId, @Param("applyType") String applyType);
}
