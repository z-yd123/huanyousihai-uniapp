package com.ruoyi.hys.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务参数（Q-xx 待确认项全部走此表，不硬编码）。
 */
public class HysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long configId;
    private String configKey;
    private String configValue;
    private String configName;
    private String remark;
    private LocalDateTime updateTime;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
