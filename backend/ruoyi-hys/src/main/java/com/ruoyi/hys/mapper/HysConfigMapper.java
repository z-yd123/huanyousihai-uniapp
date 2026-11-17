package com.ruoyi.hys.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hys.domain.HysConfig;

/**
 * 业务参数数据访问。
 */
public interface HysConfigMapper {

    List<HysConfig> selectConfigList(HysConfig query);

    HysConfig selectConfigByKey(String configKey);

    int updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    int insertConfig(HysConfig config);
}
