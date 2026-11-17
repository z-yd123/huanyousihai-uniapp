package com.ruoyi.hys.controller.admin;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hys.domain.HysAlbum;
import com.ruoyi.hys.domain.dto.HysAdminDtos;
import com.ruoyi.hys.service.HysAlbumService;

/**
 * 管理端 · AI 智能相册（运营代传素材）。
 */
@RestController
@RequestMapping("/hys/album")
public class HysAdminAlbumController extends BaseController {

    private final HysAlbumService albumService;

    public HysAdminAlbumController(HysAlbumService albumService) {
        this.albumService = albumService;
    }

    @PreAuthorize("@ss.hasPermi('hys:album:list')")
    @GetMapping("/list")
    public TableDataInfo list(HysAlbum query) {
        startPage();
        List<HysAlbum> list = albumService.adminList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('hys:album:list')")
    @GetMapping("/{albumId}")
    public AjaxResult getInfo(@PathVariable Long albumId) {
        HysAlbum query = new HysAlbum();
        query.setAlbumId(albumId);
        return success(albumService.adminList(query).stream().findFirst().orElse(null));
    }

    @PreAuthorize("@ss.hasPermi('hys:album:edit')")
    @Log(title = "环游四海相册", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HysAdminDtos.AlbumSave body) {
        return success(albumService.save(body));
    }

    @PreAuthorize("@ss.hasPermi('hys:album:edit')")
    @Log(title = "环游四海相册", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HysAdminDtos.AlbumSave body) {
        return success(albumService.save(body));
    }

    @PreAuthorize("@ss.hasPermi('hys:album:edit')")
    @Log(title = "环游四海相册", businessType = BusinessType.DELETE)
    @DeleteMapping("/{albumId}")
    public AjaxResult remove(@PathVariable Long albumId) {
        albumService.delete(albumId);
        return success("已删除");
    }
}
