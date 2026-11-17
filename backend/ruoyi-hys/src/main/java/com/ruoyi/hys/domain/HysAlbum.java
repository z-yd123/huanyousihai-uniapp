package com.ruoyi.hys.domain;

import java.time.LocalDate;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI 智能相册（398 权益的独立子系统数据源）。素材以 JSON 数组存一条记录，避免多表联查。
 */
public class HysAlbum extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long albumId;
    private Long memberId;
    private Long productId;
    private String title;
    private LocalDate travelDate;
    private String cover;
    /** [{category:HOTEL|FOOD|SCENIC|VIDEO, url, title}] */
    private String mediaJson;
    private String description;
    private String status;

    private String memberName;
    private String productTitle;
    /** 素材按分组解析后的结果（非持久化） */
    private List<Media> mediaList;

    /**
     * 单条素材。
     *
     * @param category HOTEL / FOOD / SCENIC / VIDEO
     * @param url      素材地址
     * @param title    素材标题
     */
    public record Media(String category, String url, String title) {
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getMediaJson() {
        return mediaJson;
    }

    public void setMediaJson(String mediaJson) {
        this.mediaJson = mediaJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }
}
