package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hys.domain.HysConfig;
import com.ruoyi.hys.domain.dto.HysAdminDtos;
import com.ruoyi.hys.service.HysConfigService;

/**
 * 管理端 · 业务参数（Q-xx 待确认项都在这里改，不用改代码）。
 */
@RestController
@RequestMapping("/hys/config")
public class HysAdminConfigController extends BaseController {

    private final HysConfigService configService;

    public HysAdminConfigController(HysConfigService configService) {
        this.configService = configService;
    }

    @PreAuthorize("@ss.hasPermi('hys:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysConfig query) {
        startPage();
        List<HysConfig> list = configService.selectConfigList(query);
        return getDataTable(list);
    }

    /**
     * 修改参数：服务端会做红线校验（拨比总额、手续费率、充值上限等）。
     */
    @PreAuthorize("@ss.hasPermi('hys:config:edit')")
    @Log(title = "环游四海业务参数", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HysAdminDtos.ConfigUpdate body) {
        configService.update(body.configKey(), body.configValue());
        return success("已保存");
    }
}
