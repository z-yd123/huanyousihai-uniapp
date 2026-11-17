package com.ruoyi.hys.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.vo.HysVos;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 邀请二维码生成。
 *
 * <p>
 * 用 zxing 在服务端出 PNG，再以 data URL 返回：小程序端直接 {@code <image :src="...">} 即可，
 * 不依赖微信 access_token（本期未接入小程序码接口，见 docs/01 待确认项说明）。
 * 二维码内容为 {@code 邀请域名/r/{手机号}}，扫码进入注册页并自动带入推荐人。
 * </p>
 */
@Service
public class HysQrCodeService {

    private static final Logger log = LoggerFactory.getLogger(HysQrCodeService.class);

    private static final int DEFAULT_SIZE = 480;

    private final HysConfigService configService;

    public HysQrCodeService(HysConfigService configService) {
        this.configService = configService;
    }

    /**
     * 生成当前会员的邀请二维码。
     *
     * @param phone 会员手机号（即邀请码）
     * @param size  边长（像素），限制在 200~1000，防止被用来打大图耗 CPU
     */
    public HysVos.InviteQr inviteQr(String phone, Integer size) {
        int px = size == null ? DEFAULT_SIZE : Math.max(200, Math.min(1000, size));
        String base = configService.getString("app.invite.base", "https://hys.example.com/r/");
        String content = base.endsWith("/") ? base + phone : base + "/" + phone;
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, px, px, hints);
            BufferedImage image = new BufferedImage(px, px, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < px; x++) {
                for (int y = 0; y < px; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0x1C2B26 : 0xFFFDF8);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            String dataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
            return new HysVos.InviteQr(phone, content, dataUrl);
        } catch (Exception e) {
            log.error("生成邀请二维码失败", e);
            throw new ServiceException("二维码生成失败，请稍后重试");
        }
    }
}
