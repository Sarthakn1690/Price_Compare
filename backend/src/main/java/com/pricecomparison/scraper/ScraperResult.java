package com.pricecomparison.scraper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScraperResult {

    private String name;
    private String brand;
    private String category;
    private String imageUrl;
    private Map<String, String> specifications;
    private BigDecimal price;
    private String productUrl;
    private boolean availability;
    private String platform;
}
