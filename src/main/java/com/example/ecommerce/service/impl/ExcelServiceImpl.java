package com.example.ecommerce.service.impl;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.excel.OrderExcelModel;
import com.example.ecommerce.excel.ProductExcelModel;
import com.example.ecommerce.service.ExcelService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.util.ExcelUtil;
import com.example.ecommerce.vo.OrderExportVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelServiceImpl implements ExcelService {
    private final ProductService productService;
    private final OrderService orderService;

    public ExcelServiceImpl(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    public int importProducts(MultipartFile file) throws IOException {
        List<ProductExcelModel> rows = ExcelUtil.read(file, ProductExcelModel.class);
        List<Product> products = rows.stream().map(this::toProduct).toList();
        productService.saveBatch(products);
        return products.size();
    }

    public void exportProducts(HttpServletResponse response) throws IOException {
        List<ProductExcelModel> rows = productService.list().stream().map(this::toProductExcel).toList();
        ExcelUtil.write(response, "products", "商品数据", ProductExcelModel.class, rows);
    }

    public void exportOrders(HttpServletResponse response, Integer status) throws IOException {
        List<OrderExcelModel> rows = orderService.exportOrders(status).stream().map(this::toOrderExcel).toList();
        ExcelUtil.write(response, "orders", "订单数据", OrderExcelModel.class, rows);
    }

    private Product toProduct(ProductExcelModel row) {
        Product product = new Product();
        product.setCategoryId(row.getCategoryId());
        product.setName(row.getName());
        product.setSubtitle(row.getSubtitle());
        product.setMainImage(row.getMainImage());
        product.setDetail(row.getDetail());
        product.setPrice(row.getPrice());
        product.setOriginalPrice(row.getOriginalPrice());
        product.setStock(row.getStock());
        product.setSales(0);
        product.setUnit(row.getUnit());
        product.setStatus(row.getStatus() == null ? 0 : row.getStatus());
        product.setSort(row.getSort() == null ? 0 : row.getSort());
        product.setDeleted(0);
        return product;
    }

    private ProductExcelModel toProductExcel(Product product) {
        ProductExcelModel row = new ProductExcelModel();
        row.setCategoryId(product.getCategoryId());
        row.setName(product.getName());
        row.setSubtitle(product.getSubtitle());
        row.setMainImage(product.getMainImage());
        row.setDetail(product.getDetail());
        row.setPrice(product.getPrice());
        row.setOriginalPrice(product.getOriginalPrice());
        row.setStock(product.getStock());
        row.setUnit(product.getUnit());
        row.setStatus(product.getStatus());
        row.setSort(product.getSort());
        return row;
    }

    private OrderExcelModel toOrderExcel(OrderExportVO vo) {
        OrderExcelModel row = new OrderExcelModel();
        row.setOrderNo(vo.getOrderNo());
        row.setUserId(vo.getUserId());
        row.setOrderStatus(vo.getOrderStatus());
        row.setPayAmount(vo.getPayAmount());
        row.setReceiverName(vo.getReceiverName());
        row.setReceiverPhone(vo.getReceiverPhone());
        row.setCreateTime(vo.getCreateTime());
        return row;
    }
}
