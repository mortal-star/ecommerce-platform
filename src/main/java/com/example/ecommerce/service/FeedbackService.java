package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.FeedbackReplyDTO;
import com.example.ecommerce.entity.Feedback;
import com.example.ecommerce.vo.PageVO;

public interface FeedbackService extends IService<Feedback> {
    Feedback submitFeedback(Long userId, Feedback feedback);
    PageVO<Feedback> listFeedbacks(Long pageNum, Long pageSize, Integer status);
    void replyFeedback(Long id, FeedbackReplyDTO request);
    void markHandled(Long id);
}
