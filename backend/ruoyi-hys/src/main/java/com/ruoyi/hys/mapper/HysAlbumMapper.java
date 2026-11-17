package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysAlbum;

/**
 * 相册数据访问。
 */
public interface HysAlbumMapper {

    List<HysAlbum> selectAlbumList(HysAlbum query);

    HysAlbum selectAlbumById(Long albumId);

    int insertAlbum(HysAlbum album);

    int updateAlbum(HysAlbum album);

    int deleteAlbumById(Long albumId);
}
