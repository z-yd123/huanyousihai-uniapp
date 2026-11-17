package com.ruoyi.hys.domain;

import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 商品 / 线路（线路即商品，线路的每日行程见 {@link HysRouteDay}）。
 */
public class HysProduct extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long productId;
    /** ROUTE / SPECIAL / VIP_RIGHTS */
    private String productType;
    /** GROUP 组团游 / SELF_DRIVE 自驾游 */
    private String routeType;
    private String title;
    private String subtitle;
    private String cover;
    /** 图集 JSON 数组字符串 */
    private String images;
    private String videoUrl;
    private String detail;
    /** 普通会员价（分） */
    private Long normalPrice;
    /** VIP 会员价（分） */
    private Long vipPrice;
    private Integer stock;
    private Integer sales;
    private Integer days;
    private Integer nights;
    private String departCity;
    private String gatherTime;
    private String gatherPlace;
    private String includes;
    private String excludes;
    private String status;
    private Integer sort;

    /** 详情页行程分段（非持久化） */
    private List<HysRouteDay> dayList;
    /** 图集解析结果（非持久化） */
    private List<String> imageList;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Long getNormalPrice() {
        return normalPrice;
    }

    public void setNormalPrice(Long normalPrice) {
        this.normalPrice = normalPrice;
    }

    public Long getVipPrice() {
        return vipPrice;
    }

    public void setVipPrice(Long vipPrice) {
        this.vipPrice = vipPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getNights() {
        return nights;
    }

    public void setNights(Integer nights) {
        this.nights = nights;
    }

    public String getDepartCity() {
        return departCity;
    }

    public void setDepartCity(String departCity) {
        this.departCity = departCity;
    }

    public String getGatherTime() {
        return gatherTime;
    }

    public void setGatherTime(String gatherTime) {
        this.gatherTime = gatherTime;
    }

    public String getGatherPlace() {
        return gatherPlace;
    }

    public void setGatherPlace(String gatherPlace) {
        this.gatherPlace = gatherPlace;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<HysRouteDay> getDayList() {
        return dayList;
    }

    public void setDayList(List<HysRouteDay> dayList) {
        this.dayList = dayList;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }
}
