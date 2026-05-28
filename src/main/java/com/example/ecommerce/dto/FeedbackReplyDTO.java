package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public class FeedbackReplyDTO {
    @NotBlank(message = "回复内容不能为空")
    private String reply;

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
