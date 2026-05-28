package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {
    Notice publishNotice(Notice notice);
    Notice updateNotice(Long id, Notice notice);
    void deleteNotice(Long id);
    List<Notice> listNotices(Integer status);
}
