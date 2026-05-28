package com.example.ecommerce.service;

public interface EmailCodeService {

    String sendCode(String email, String scene);

    void verifyCode(String email, String scene, String code);
}
