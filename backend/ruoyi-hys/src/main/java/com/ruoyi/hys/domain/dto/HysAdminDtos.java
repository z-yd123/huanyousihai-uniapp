package com.ruoyi.hys.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 管理端请求体。
 */
public final class HysAdminDtos {

    private HysAdminDtos() {
    }

    /**
     * 资金审核（充值 / 提现通用）。
     *
     * @param pass   是否通过
     * @param remark 审核意见
     */
    public record Audit(
            @NotNull(message = "请选择审核结果") Boolean pass,
            @Size(max = 100, message = "审核意见不能超过 100 个字") String remark) {
    }

    /**
     * 补绑推荐人。仅允许对「推荐关系异常」的账号使用；补绑后自动重算历史未发放佣金。
     *
     * @param referrerPhone 新推荐人手机号
     * @param remark        处理说明
     */
    public record Rebind(
            @NotBlank(message = "请填写新推荐人手机号") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "推荐人手机号格式不正确") String referrerPhone,
            @Size(max = 100, message = "处理说明不能超过 100 个字") String remark) {
    }

    /**
     * 会员资料维护（仅允许改名称/区域/状态；金额与推荐关系不提供直接编辑入口）。
     */
    public record MemberEdit(
            @NotBlank(message = "请填写名称") @Size(max = 20, message = "名称不能超过 20 个字") String nickname,
            @NotBlank(message = "请选择省份") String province,
            @NotBlank(message = "请选择城市") String city,
            @NotBlank(message = "请选择区/县") String district,
            @NotBlank(message = "请选择状态") String status,
            @Size(max = 100, message = "备注不能超过 100 个字") String remark) {
    }

    /**
     * 业务参数修改。
     */
    public record ConfigUpdate(
            @NotBlank(message = "缺少参数键") String configKey,
            @NotBlank(message = "缺少参数值") @Size(max = 200) String configValue) {
    }

    /**
     * 相册素材（运营代传）。
     *
     * @param albumId   相册ID（修改时必填）
     * @param memberId  所属会员
     * @param productId 关联线路
     * @param title     批次标题
     * @param travelDate 出行日期
     * @param cover     封面
     * @param mediaJson 素材 JSON
     * @param description 回忆文案
     * @param status    0 可浏览 / 1 隐藏（为空时按可浏览处理）
     */
    public record AlbumSave(
            Long albumId,
            @NotNull(message = "请选择所属会员") Long memberId,
            Long productId,
            @NotBlank(message = "请填写批次标题") @Size(max = 60) String title,
            String travelDate,
            String cover,
            String mediaJson,
            @Size(max = 200) String description,
            String status) {
    }
}
