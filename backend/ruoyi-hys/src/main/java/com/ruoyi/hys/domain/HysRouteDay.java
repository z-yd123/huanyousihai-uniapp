package com.ruoyi.hys.domain;

import java.io.Serializable;

/**
 * 线路每日行程（P-02 行程票根的按天分段数据源）。
 */
public class HysRouteDay implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dayId;
    private Long productId;
    private Integer dayNo;
    private String title;
    private String content;
    /** 当日配图 JSON 数组 */
    private String images;
    private String gatherTime;
    private String gatherPlace;

    public Long getDayId() {
        return dayId;
    }

    public void setDayId(Long dayId) {
        this.dayId = dayId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getDayNo() {
        return dayNo;
    }

    public void setDayNo(Integer dayNo) {
        this.dayNo = dayNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
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
}
