package com.example.ecommerce.util;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ExcelUtil {

    private ExcelUtil() {
    }

    public static <T> List<T> read(MultipartFile file, Class<T> headClass) throws IOException {
        return EasyExcel.read(file.getInputStream()).head(headClass).sheet().doReadSync();
    }

    public static <T> void write(HttpServletResponse response, String fileName, String sheetName, Class<T> headClass, List<T> data) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encodedFileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), headClass).sheet(sheetName).doWrite(data);
    }
}
