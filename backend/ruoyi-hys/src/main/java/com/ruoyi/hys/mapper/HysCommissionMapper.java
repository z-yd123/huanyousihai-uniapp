package com.ruoyi.hys.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hys.domain.HysCommission;

/**
 * 佣金数据访问。层级枚举仅 DIRECT / INDIRECT，SQL 不做任何层级扩展。
 */
public interface HysCommissionMapper {

    List<HysCommission> selectCommissionList(HysCommission query);

    List<HysCommission> selectByOrderId(Long orderId);

    int insertCommission(HysCommission commission);

    long sumAmount(@Param("memberId") Long memberId, @Param("level") String level, @Param("status") Integer status);

    int countByMember(@Param("memberId") Long memberId, @Param("level") String level);

    int updateStatus(@Param("commissionId") Long commissionId, @Param("status") Integer status);

    long sumTotal();

    long sumToday(@Param("start") LocalDateTime start);

    long countByStatus(@Param("status") Integer status);
}
