package com.ruoyi.hys.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.config.HysConstants;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.domain.HysOrder;
import com.ruoyi.hys.domain.HysProduct;
import com.ruoyi.hys.domain.HysRouteDay;
import com.ruoyi.hys.domain.vo.HysVos;
import com.ruoyi.hys.mapper.HysOrderMapper;
import com.ruoyi.hys.mapper.HysProductMapper;
import com.ruoyi.hys.mapper.HysRouteDayMapper;

/**
 * 线路 / 商品服务。
 *
 * <p>
 * 双价取价规则（AGENT.md §4.5、Q-04）：<b>只有 VIP 在有效期内</b>才取 VIP 价，
 * 过期后一律回落普通价——判断集中在 {@link #isVipActive}，不在前端做任何价格推断，
 * 避免“改个本地缓存就能用 VIP 价下单”的漏洞。下单时价格还会在服务端重新计算并落快照。
 * </p>
 */
@Service
public class HysCatalogService {

    private final HysProductMapper productMapper;

    private final HysRouteDayMapper routeDayMapper;

    private final HysOrderMapper orderMapper;

    private final ObjectMapper objectMapper;

    public HysCatalogService(HysProductMapper productMapper, HysRouteDayMapper routeDayMapper,
            HysOrderMapper orderMapper, ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.routeDayMapper = routeDayMapper;
        this.orderMapper = orderMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * VIP 是否在有效期内。
     */
    public static boolean isVipActive(HysMember member) {
        return member != null
                && HysConstants.LEVEL_VIP.equals(member.getLevel())
                && member.getVipExpireAt() != null
                && member.getVipExpireAt().isAfter(LocalDateTime.now());
    }

    /**
     * 首页聚合：一次请求返回 398 权益 + 线路 + 特产，减少小程序首屏串行请求。
     */
    public HysVos.Home home(HysMember member, String notice) {
        boolean vip = isVipActive(member);
        HysProduct vipQuery = new HysProduct();
        vipQuery.setProductType(HysConstants.PRODUCT_VIP_RIGHTS);
        vipQuery.setStatus(HysConstants.ON_SALE);
        List<HysProduct> vipRights = productMapper.selectProductList(vipQuery);

        HysProduct routeQuery = new HysProduct();
        routeQuery.setProductType(HysConstants.PRODUCT_ROUTE);
        routeQuery.setStatus(HysConstants.ON_SALE);
        List<HysProduct> routes = productMapper.selectProductList(routeQuery);

        HysProduct specialQuery = new HysProduct();
        specialQuery.setProductType(HysConstants.PRODUCT_SPECIAL);
        specialQuery.setStatus(HysConstants.ON_SALE);
        List<HysProduct> specials = productMapper.selectProductList(specialQuery);

        return new HysVos.Home(
                vipRights.isEmpty() ? null : toCard(vipRights.get(0), vip),
                routes.stream().map(p -> toCard(p, vip)).toList(),
                specials.stream().map(p -> toCard(p, vip)).toList(),
                notice);
    }

    public List<HysProduct> selectList(HysProduct query) {
        return productMapper.selectProductList(query);
    }

    /**
     * 商品 / 线路详情。
     */
    public HysVos.ProductDetail detail(Long productId, HysMember member) {
        HysProduct product = productMapper.selectProductById(productId);
        if (product == null) {
            throw new ServiceException("线路不存在或已下架");
        }
        boolean vip = isVipActive(member);
        List<HysRouteDay> days = routeDayMapper.selectDaysByProductId(productId);
        List<HysVos.DayItem> dayItems = new ArrayList<>(days.size());
        for (HysRouteDay day : days) {
            dayItems.add(new HysVos.DayItem(day.getDayNo(), day.getTitle(), day.getContent(),
                    parseImages(day.getImages()), day.getGatherTime(), day.getGatherPlace()));
        }
        return new HysVos.ProductDetail(
                product.getProductId(), product.getProductType(), product.getRouteType(), product.getTitle(),
                product.getSubtitle(), product.getCover(), parseImages(product.getImages()), product.getVideoUrl(),
                product.getDetail(), priceFor(product, vip), product.getNormalPrice(), product.getVipPrice(),
                vip && product.getVipPrice() != null, product.getStock(), product.getSales(), product.getDays(),
                product.getNights(), product.getDepartCity(), product.getGatherTime(), product.getGatherPlace(),
                product.getIncludes(), product.getExcludes(), dayItems);
    }

    /**
     * 下单取价（服务端唯一权威口径）。
     */
    public long priceFor(HysProduct product, boolean vipActive) {
        if (vipActive && product.getVipPrice() != null && product.getVipPrice() > 0) {
            return product.getVipPrice();
        }
        return product.getNormalPrice() == null ? 0L : product.getNormalPrice();
    }

    // ------------------------------------------------------------------
    // 后台维护
    // ------------------------------------------------------------------

    public HysProduct adminGet(Long productId) {
        HysProduct product = productMapper.selectProductById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        product.setImageList(parseImages(product.getImages()));
        product.setDayList(routeDayMapper.selectDaysByProductId(productId));
        return product;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(HysProduct product) {
        validateProduct(product);
        if (product.getImages() == null && product.getImageList() != null) {
            product.setImages(writeJson(product.getImageList()));
        }
        if (product.getProductId() == null) {
            productMapper.insertProduct(product);
        } else {
            productMapper.updateProduct(product);
        }
        saveDays(product);
        return product.getProductId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long productId, String status) {
        HysProduct patch = new HysProduct();
        patch.setProductId(productId);
        patch.setStatus(status);
        productMapper.updateProduct(patch);
    }

    /**
     * 删除：已被订单引用的商品不允许删除（订单需要保留商品快照与追溯能力），只能下架。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long productId) {
        long referenced = orderMapper.countByProductId(productId);
        if (referenced > 0) {
            throw new ServiceException("该商品已有 " + referenced + " 笔订单，不能删除，请改为下架");
        }
        routeDayMapper.deleteDaysByProductId(productId);
        productMapper.deleteProductById(productId);
    }

    private void validateProduct(HysProduct product) {
        if (product.getTitle() == null || product.getTitle().isBlank()) {
            throw new ServiceException("请填写标题");
        }
        if (product.getProductType() == null || product.getProductType().isBlank()) {
            throw new ServiceException("请选择商品类型");
        }
        if (HysConstants.PRODUCT_VIP_RIGHTS.equals(product.getProductType())
                && (product.getNormalPrice() == null || product.getNormalPrice() != 39_800L)) {
            throw new ServiceException("会员权益商品对外口径固定 398 元，不允许改价");
        }
        if (product.getNormalPrice() == null || product.getNormalPrice() <= 0) {
            throw new ServiceException("普通会员价必须大于 0");
        }
        if (product.getVipPrice() != null && product.getVipPrice() > product.getNormalPrice()) {
            throw new ServiceException("VIP 价不能高于普通价");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new ServiceException("库存不能为负");
        }
    }

    private void saveDays(HysProduct product) {
        if (product.getDayList() == null) {
            return;
        }
        routeDayMapper.deleteDaysByProductId(product.getProductId());
        List<HysRouteDay> days = new ArrayList<>();
        int index = 1;
        for (HysRouteDay day : product.getDayList()) {
            if (day.getTitle() == null || day.getTitle().isBlank()) {
                continue;
            }
            HysRouteDay item = new HysRouteDay();
            item.setProductId(product.getProductId());
            item.setDayNo(index++);
            item.setTitle(day.getTitle());
            item.setContent(day.getContent());
            item.setImages(day.getImages() == null || day.getImages().isBlank() ? "[]" : day.getImages());
            item.setGatherTime(day.getGatherTime());
            item.setGatherPlace(day.getGatherPlace());
            days.add(item);
        }
        if (!days.isEmpty()) {
            routeDayMapper.batchInsertDays(days);
        }
    }

    // ------------------------------------------------------------------
    // 转换工具
    // ------------------------------------------------------------------

    public HysVos.ProductCard toCard(HysProduct product, boolean vipActive) {
        String tag = switch (product.getProductType()) {
            case HysConstants.PRODUCT_ROUTE -> HysConstants.ROUTE_SELF_DRIVE.equals(product.getRouteType())
                    ? "自驾游" : "组团游";
            case HysConstants.PRODUCT_VIP_RIGHTS -> "398 权益";
            default -> "特产";
        };
        return new HysVos.ProductCard(product.getProductId(), product.getProductType(), product.getRouteType(),
                product.getTitle(), product.getSubtitle(), product.getCover(), priceFor(product, vipActive),
                product.getNormalPrice(), product.getVipPrice(), vipActive && product.getVipPrice() != null,
                product.getSales(), product.getDays(), product.getNights(), tag);
    }

    public List<String> parseImages(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            // 素材配置是人填的，容错为「不展示」而不是整个详情 500
            return List.of();
        }
    }

    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new ServiceException("素材数据格式不合法");
        }
    }
}
