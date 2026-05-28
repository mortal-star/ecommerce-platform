package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.entity.Notice;
import com.example.ecommerce.mapper.NoticeMapper;
import com.example.ecommerce.service.NoticeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    public Notice publishNotice(Notice notice) {
        notice.setDeleted(0);
        if (notice.getStatus() == null) notice.setStatus(1);
        if (notice.getSort() == null) notice.setSort(0);
        if (notice.getPublishTime() == null) notice.setPublishTime(LocalDateTime.now());
        save(notice);
        return notice;
    }
    public Notice updateNotice(Long id, Notice notice) {
        notice.setId(id);
        updateById(notice);
        return getById(id);
    }
    public void deleteNotice(Long id) { removeById(id); }
    public List<Notice> listNotices(Integer status) {
        return list(new LambdaQueryWrapper<Notice>()
                .eq(status != null, Notice::getStatus, status)
                .eq(Notice::getDeleted, 0)
                .orderByAsc(Notice::getSort)
                .orderByDesc(Notice::getPublishTime));
    }
}
