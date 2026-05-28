package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/admin/excel")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExcelController {

    private final ExcelService excelService;

    public AdminExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/products/import")
    public Result<Map<String, Integer>> importProducts(@RequestParam("file") MultipartFile file) throws IOException {
        int count = excelService.importProducts(file);
        return Result.success("商品导入成功", Map.of("count", count));
    }

    @GetMapping("/products/export")
    public void exportProducts(HttpServletResponse response) throws IOException {
        excelService.exportProducts(response);
    }

    @GetMapping("/orders/export")
    public void exportOrders(HttpServletResponse response, @RequestParam(required = false) Integer status) throws IOException {
        excelService.exportOrders(response, status);
    }
}
