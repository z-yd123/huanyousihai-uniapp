package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hys.domain.HysProduct;
import com.ruoyi.hys.service.HysCatalogService;

/**
 * 管理端 · 线路与商品。
 */
@RestController
@RequestMapping("/hys/product")
public class HysAdminProductController extends BaseController {

    private final HysCatalogService catalogService;

    public HysAdminProductController(HysCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PreAuthorize("@ss.hasPermi('hys:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysProduct query) {
        startPage();
        List<HysProduct> list = catalogService.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:product:query')")
    @GetMapping("/{productId}")
    public AjaxResult getInfo(@PathVariable Long productId) {
        return success(catalogService.adminGet(productId));
    }

    @PreAuthorize("@ss.hasPermi('hys:product:add')")
    @Log(title = "环游四海商品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HysProduct product) {
        return success(catalogService.save(product));
    }

    @PreAuthorize("@ss.hasPermi('hys:product:edit')")
    @Log(title = "环游四海商品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HysProduct product) {
        return success(catalogService.save(product));
    }

    /**
     * 上架 / 下架。
     */
    @PreAuthorize("@ss.hasPermi('hys:product:edit')")
    @Log(title = "环游四海商品", businessType = BusinessType.UPDATE)
    @PutMapping("/{productId}/status")
    public AjaxResult changeStatus(@PathVariable Long productId, @RequestParam String status) {
        catalogService.changeStatus(productId, status);
        return success("0".equals(status) ? "已上架" : "已下架");
    }

    /**
     * 删除（有订单引用时拒绝，只能下架）。
     */
    @PreAuthorize("@ss.hasPermi('hys:product:remove')")
    @Log(title = "环游四海商品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productId}")
    public AjaxResult remove(@PathVariable Long productId) {
        catalogService.delete(productId);
        return success("已删除");
    }
}
