package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.FeedbackReplyDTO;
import com.example.ecommerce.entity.Feedback;
import com.example.ecommerce.mapper.FeedbackMapper;
import com.example.ecommerce.service.FeedbackService;
import com.example.ecommerce.vo.PageVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
    public Feedback submitFeedback(Long userId, Feedback feedback) {
        feedback.setUserId(userId);
        feedback.setStatus(0);
        feedback.setDeleted(0);
        save(feedback);
        return feedback;
    }
    public PageVO<Feedback> listFeedbacks(Long pageNum, Long pageSize, Integer status) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 100L);
        Page<Feedback> page = page(new Page<>(current, size), new LambdaQueryWrapper<Feedback>()
                .eq(status != null, Feedback::getStatus, status)
                .eq(Feedback::getDeleted, 0)
                .orderByDesc(Feedback::getCreateTime));
        return new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }
    public void replyFeedback(Long id, FeedbackReplyDTO request) {
        Feedback feedback = getById(id);
        if (feedback == null || !Integer.valueOf(0).equals(feedback.getDeleted())) throw new IllegalArgumentException("反馈不存在");
        feedback.setReply(request.getReply());
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setStatus(1);
        updateById(feedback);
    }
    public void markHandled(Long id) {
        Feedback feedback = getById(id);
        if (feedback == null || !Integer.valueOf(0).equals(feedback.getDeleted())) throw new IllegalArgumentException("反馈不存在");
        feedback.setStatus(1);
        updateById(feedback);
    }
}
