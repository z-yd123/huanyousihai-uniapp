package com.ruoyi.hys.controller.app;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.config.HysMemberContext;
import com.ruoyi.hys.domain.HysAlbum;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysProduct;
import com.ruoyi.hys.service.HysAlbumService;
import com.ruoyi.hys.service.HysCatalogService;
import com.ruoyi.hys.service.HysConfigService;

/**
 * 小程序 · 首页 / 线路 / 商城 / 相册。
 */
@Anonymous
@RestController
@RequestMapping("/app")
public class HysAppCatalogController extends BaseController {

    private final HysCatalogService catalogService;

    private final HysAlbumService albumService;

    private final HysConfigService configService;

    public HysAppCatalogController(HysCatalogService catalogService, HysAlbumService albumService,
            HysConfigService configService) {
        this.catalogService = catalogService;
        this.albumService = albumService;
        this.configService = configService;
    }

    /**
     * 首页聚合：398 权益 + 线路 + 特产 + 合规提示。
     */
    @GetMapping("/home")
    public AjaxResult home() {
        HysMember member = HysMemberContext.require();
        return success(catalogService.home(member, configService.appNotice()));
    }

    /**
     * 列表（按类型 / 线路类型 / 关键词筛选，分页）。
     */
    @GetMapping("/products")
    public TableDataInfo products(HysProduct query) {
        HysMember member = HysMemberContext.require();
        // 小程序端只能看到上架商品，不接受前端传 status
        query.setStatus(HysConstants.ON_SALE);
        startPage();
        List<HysProduct> list = catalogService.selectList(query);
        boolean vip = HysCatalogService.isVipActive(member);
        List<?> cards = list.stream().map(product -> catalogService.toCard(product, vip)).toList();
        return getDataTable(cards);
    }

    /**
     * 线路 / 商品详情。
     */
    @GetMapping("/products/{productId}")
    public AjaxResult detail(@PathVariable Long productId) {
        HysMember member = HysMemberContext.require();
        return success(catalogService.detail(productId, member));
    }

    /**
     * 我的相册（398 权益内容）。
     */
    @GetMapping("/albums")
    public AjaxResult albums() {
        Long memberId = HysMemberContext.requireMemberId();
        return success(albumService.listByMember(memberId));
    }

    /**
     * 相册详情。
     */
    @GetMapping("/albums/{albumId}")
    public AjaxResult albumDetail(@PathVariable Long albumId) {
        Long memberId = HysMemberContext.requireMemberId();
        HysAlbum album = albumService.getForMember(memberId, albumId);
        return success(album);
    }
}
