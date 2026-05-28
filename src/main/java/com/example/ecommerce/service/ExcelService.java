package com.example.ecommerce.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExcelService {
    int importProducts(MultipartFile file) throws IOException;
    void exportProducts(HttpServletResponse response) throws IOException;
    void exportOrders(HttpServletResponse response, Integer status) throws IOException;
}
