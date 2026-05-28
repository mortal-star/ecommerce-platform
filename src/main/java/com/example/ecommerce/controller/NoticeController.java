package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.entity.Notice;
import com.example.ecommerce.service.NoticeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notices")
public class NoticeController {
    private final NoticeService noticeService;
    public NoticeController(NoticeService noticeService) { this.noticeService = noticeService; }

    @GetMapping
    public Result<List<Notice>> list(@RequestParam(required = false) Integer status) { return Result.success(noticeService.listNotices(status)); }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Notice> publish(@RequestBody Notice notice) { return Result.success("公告发布成功", noticeService.publishNotice(notice)); }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Notice> update(@PathVariable Long id, @RequestBody Notice notice) { return Result.success("公告修改成功", noticeService.updateNotice(id, notice)); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) { noticeService.deleteNotice(id); return Result.success("公告删除成功", null); }
}
