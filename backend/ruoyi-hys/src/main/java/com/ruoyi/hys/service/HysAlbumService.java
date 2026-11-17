package com.ruoyi.hys.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hys.domain.HysAlbum;
import com.ruoyi.hys.domain.dto.HysAdminDtos;
import com.ruoyi.hys.mapper.HysAlbumMapper;

/**
 * AI 智能相册服务（398 权益的落地形态）。
 *
 * <p>
 * 数据边界：小程序端只允许读<b>自己的</b>相册，{@code memberId} 一律取自令牌，不接受前端传入。
 * </p>
 */
@Service
public class HysAlbumService {

    private final HysAlbumMapper albumMapper;

    private final ObjectMapper objectMapper;

    public HysAlbumService(HysAlbumMapper albumMapper, ObjectMapper objectMapper) {
        this.albumMapper = albumMapper;
        this.objectMapper = objectMapper;
    }

    public List<HysAlbum> listByMember(Long memberId) {
        HysAlbum query = new HysAlbum();
        query.setMemberId(memberId);
        query.setStatus("0");
        return albumMapper.selectAlbumList(query).stream().map(this::withMedia).toList();
    }

    public HysAlbum getForMember(Long memberId, Long albumId) {
        HysAlbum album = albumMapper.selectAlbumById(albumId);
        if (album == null || !album.getMemberId().equals(memberId)) {
            throw new ServiceException("相册不存在或无权查看");
        }
        return withMedia(album);
    }

    public List<HysAlbum> adminList(HysAlbum query) {
        return albumMapper.selectAlbumList(query).stream().map(this::withMedia).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(HysAdminDtos.AlbumSave body) {
        HysAlbum album = new HysAlbum();
        album.setAlbumId(body.albumId());
        album.setMemberId(body.memberId());
        album.setProductId(body.productId());
        album.setTitle(body.title());
        album.setTravelDate(parseDate(body.travelDate()));
        album.setCover(body.cover());
        album.setMediaJson(body.mediaJson() == null || body.mediaJson().isBlank() ? "[]" : body.mediaJson());
        album.setDescription(body.description());
        // 未显式指定时按「可浏览」入库，与表默认值保持一致
        album.setStatus(body.status() == null || body.status().isBlank() ? "0" : body.status());
        if (body.albumId() == null) {
            albumMapper.insertAlbum(album);
        } else {
            albumMapper.updateAlbum(album);
        }
        return album.getAlbumId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long albumId) {
        albumMapper.deleteAlbumById(albumId);
    }

    private HysAlbum withMedia(HysAlbum album) {
        album.setMediaList(parseMedia(album.getMediaJson()));
        return album;
    }

    private List<HysAlbum.Media> parseMedia(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<HysAlbum.Media>>() {
            });
        } catch (Exception e) {
            // 素材是运营手填的，格式坏了只影响这一张相册，不应让整个接口 500
            return List.of();
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
        } catch (Exception e) {
            throw new ServiceException("出行日期格式应为 yyyy-MM-dd");
        }
    }
}
