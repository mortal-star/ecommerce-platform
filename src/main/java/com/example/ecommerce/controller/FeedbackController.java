package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.FeedbackReplyDTO;
import com.example.ecommerce.entity.Feedback;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.FeedbackService;
import com.example.ecommerce.vo.PageVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Feedback> submit(@AuthenticationPrincipal SecurityUserDetails principal, @RequestBody Feedback feedback) {
        return Result.success("反馈提交成功", feedbackService.submitFeedback(principal.getUserId(), feedback));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageVO<Feedback>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                         @RequestParam(defaultValue = "10") Long pageSize,
                                         @RequestParam(required = false) Integer status) {
        return Result.success(feedbackService.listFeedbacks(pageNum, pageSize, status));
    }

    @PatchMapping("/admin/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reply(@PathVariable Long id, @Valid @RequestBody FeedbackReplyDTO request) {
        feedbackService.replyFeedback(id, request);
        return Result.success("反馈回复成功", null);
    }

    @PatchMapping("/admin/{id}/handled")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> markHandled(@PathVariable Long id) {
        feedbackService.markHandled(id);
        return Result.success("反馈已标记处理", null);
    }
}
