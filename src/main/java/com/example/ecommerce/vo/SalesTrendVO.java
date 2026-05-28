package com.example.ecommerce.vo;

import java.math.BigDecimal;
import java.util.List;

public class SalesTrendVO {
    private List<String> dates;
    private List<BigDecimal> amounts;
    private List<Long> orderCounts;

    public SalesTrendVO(List<String> dates, List<BigDecimal> amounts, List<Long> orderCounts) {
        this.dates = dates;
        this.amounts = amounts;
        this.orderCounts = orderCounts;
    }

    public List<String> getDates() { return dates; }
    public List<BigDecimal> getAmounts() { return amounts; }
    public List<Long> getOrderCounts() { return orderCounts; }
}
