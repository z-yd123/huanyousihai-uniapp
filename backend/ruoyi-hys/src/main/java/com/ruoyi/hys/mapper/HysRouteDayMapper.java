package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysRouteDay;

/**
 * 线路每日行程数据访问。
 */
public interface HysRouteDayMapper {

    List<HysRouteDay> selectDaysByProductId(Long productId);

    int deleteDaysByProductId(Long productId);

    int batchInsertDays(List<HysRouteDay> days);
}
