package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysPayoutGap;

/**
 * 佣金异常对账数据访问。正常业务不写入（表应恒为空）。
 */
public interface HysPayoutGapMapper {

    int insertGap(HysPayoutGap gap);

    List<HysPayoutGap> selectGapList(HysPayoutGap query);

    HysPayoutGap selectGapById(Long gapId);

    HysPayoutGap selectByOrderId(Long orderId);

    int updateGap(HysPayoutGap gap);

    long countUnhandled();
}
