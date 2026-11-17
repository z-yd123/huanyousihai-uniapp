package com.ruoyi.hys.domain.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 小程序端请求体。
 *
 * <p>
 * 统一放在一个容器类里，避免为每个 5 行请求体单独建文件；全部使用 record（不可变 + 自动 equals/hashCode），
 * 校验注解写在组件上，由 {@code @Valid} 触发，字段级错误统一由若依的
 * {@code GlobalExceptionHandler#handleMethodArgumentNotValidException} 转成
 * {@code AjaxResult}（code=400，msg=首个字段错误），前端可直接弹该 msg。
 * </p>
 */
public final class HysAppDtos {

    private HysAppDtos() {
    }

    /**
     * 注册。推荐人码是硬性前置条件（无推荐码不允许建档），故用 @NotBlank 而非可空。
     *
     * @param referrerCode 推荐人手机号（邀请码）
     * @param nickname     名称
     * @param phone        手机号（登录账号）
     * @param password     登录密码
     * @param province     省
     * @param city         市
     * @param district     区/县
     */
    public record Register(
            @NotBlank(message = "请填写推荐人手机号") String referrerCode,
            @NotBlank(message = "请填写名称") @Size(max = 20, message = "名称不能超过 20 个字") String nickname,
            @NotBlank(message = "请填写手机号") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
            @NotBlank(message = "请设置登录密码") @Size(min = 6, max = 20, message = "密码长度需为 6-20 位") String password,
            @NotBlank(message = "请选择省份") String province,
            @NotBlank(message = "请选择城市") String city,
            @NotBlank(message = "请选择区/县") String district) {
    }

    /**
     * 登录。
     */
    public record Login(
            @NotBlank(message = "请填写手机号") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
            @NotBlank(message = "请填写密码") String password) {
    }

    /**
     * 修改密码（登录态）。
     */
    public record ChangePassword(
            @NotBlank(message = "请填写原密码") String oldPassword,
            @NotBlank(message = "请设置新密码") @Size(min = 6, max = 20, message = "新密码长度需为 6-20 位") String newPassword) {
    }

    /**
     * 找回密码（未登录）：手机号 + 名称双因子，1.0 不接短信验证码（Q-06），故用姓名做弱校验。
     */
    public record ForgotPassword(
            @NotBlank(message = "请填写手机号") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
            @NotBlank(message = "请填写注册时的名称") String nickname,
            @NotBlank(message = "请设置新密码") @Size(min = 6, max = 20, message = "新密码长度需为 6-20 位") String newPassword) {
    }

    /**
     * 下单报名（线路与商品共用）。
     *
     * @param productId    商品ID
     * @param quantity     数量
     * @param travelDate   出行日期（线路必填）
     * @param contactName  联系人
     * @param contactPhone 联系电话
     * @param remark       备注
     */
    public record CreateOrder(
            @NotNull(message = "请选择线路或商品") Long productId,
            @NotNull(message = "请填写数量") @Min(value = 1, message = "数量至少 1 件") @Max(value = 20, message = "单笔最多 20 件") Integer quantity,
            LocalDate travelDate,
            @NotBlank(message = "请填写联系人") @Size(max = 20, message = "联系人不能超过 20 个字") String contactName,
            @NotBlank(message = "请填写联系电话") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确") String contactPhone,
            @Size(max = 100, message = "备注不能超过 100 个字") String remark) {
    }

    /**
     * 余额互转。
     *
     * @param toPhone   对方手机号
     * @param amount    金额（分）
     * @param remark    备注
     * @param requestId 客户端幂等键（重试不重复扣款）
     */
    public record Transfer(
            @NotBlank(message = "请填写对方手机号") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "对方手机号格式不正确") String toPhone,
            @NotNull(message = "请填写转账金额") @Min(value = 1, message = "金额必须大于 0") Long amount,
            @Size(max = 50, message = "备注不能超过 50 个字") String remark,
            @NotBlank(message = "缺少幂等键") @Size(max = 40) String requestId) {
    }

    /**
     * 充值登记（会员先线下打款，线上登记后由运营审核入账）。
     */
    public record Recharge(
            @NotNull(message = "请填写充值金额") @Min(value = 1, message = "金额必须大于 0") Long amount,
            @Size(max = 100, message = "备注不能超过 100 个字") String remark) {
    }

    /**
     * 提现申请。
     */
    public record Withdraw(
            @NotNull(message = "请填写提现金额") @Min(value = 100, message = "单笔提现不低于 1 元") Long amount,
            @NotBlank(message = "请填写收款人姓名") @Size(max = 20) String accountName,
            @NotBlank(message = "请填写收款账号") @Pattern(regexp = "^\\d{12,25}$", message = "收款账号格式不正确") String accountNo,
            @NotBlank(message = "请填写开户行") @Size(max = 40) String bankName) {
    }

    /**
     * 售后申请。
     */
    public record AfterSale(
            @NotBlank(message = "请填写售后原因") @Size(max = 100, message = "原因不能超过 100 个字") String reason) {
    }
}
