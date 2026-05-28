package com.example.ecommerce.vo;

import java.math.BigDecimal;

public class TopProductVO {
    private Long productId;
    private String productName;
    private String mainImage;
    private Long salesCount;
    private BigDecimal salesAmount;

    public TopProductVO(Long productId, String productName, String mainImage, Long salesCount, BigDecimal salesAmount) {
        this.productId = productId;
        this.productName = productName;
        this.mainImage = mainImage;
        this.salesCount = salesCount;
        this.salesAmount = salesAmount;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getMainImage() { return mainImage; }
    public Long getSalesCount() { return salesCount; }
    public BigDecimal getSalesAmount() { return salesAmount; }
}
