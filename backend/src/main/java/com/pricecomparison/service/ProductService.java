package com.pricecomparison.service;

import com.pricecomparison.dto.*;
import com.pricecomparison.exception.CustomExceptions;
import com.pricecomparison.model.Price;
import com.pricecomparison.model.PriceHistory;
import com.pricecomparison.model.Product;
import com.pricecomparison.model.TrackedProduct;
import com.pricecomparison.repository.PriceHistoryRepository;
import com.pricecomparison.repository.PriceRepository;
import com.pricecomparison.repository.ProductRepository;
import com.pricecomparison.repository.TrackedProductRepository;
import com.pricecomparison.scraper.ScraperResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final TrackedProductRepository trackedProductRepository;
    private final ScraperService scraperService;
    private final AIRecommendationService aiRecommendationService;

    @Transactional
    public ProductResponse searchByUrl(String url) {
        ScraperResult sourceResult = scraperService.scrapeUrl(url);
        Product product = productRepository.save(Product.builder()
                .name(sourceResult.getName())
                .brand(sourceResult.getBrand())
                .category(sourceResult.getCategory())
                .imageUrl(sourceResult.getImageUrl())
                .specifications(sourceResult.getSpecifications() != null ? sourceResult.getSpecifications() : Map.of())
                .build());
        List<Price> prices = new ArrayList<>();
        savePrice(product, sourceResult.getPlatform(), sourceResult.getPrice(), sourceResult.getProductUrl(), sourceResult.isAvailability(), prices);
        recordPriceHistory(product, sourceResult.getPlatform(), sourceResult.getPrice());
        return toProductResponse(product, enrichPricesWithSavings(prices));
    }

    private void savePrice(Product product, String platform, BigDecimal price, String productUrl, boolean availability, List<Price> out) {
        Price p = priceRepository.save(Price.builder()
                .product(product)
                .platform(platform)
                .price(price)
                .currency("INR")
                .productUrl(productUrl)
                .availability(availability)
                .build());
        out.add(p);
    }

    private void recordPriceHistory(Product product, String platform, BigDecimal price) {
        priceHistoryRepository.save(PriceHistory.builder()
                .product(product)
                .platform(platform)
                .price(price)
                .build());
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.ProductNotFoundException(id));
        List<Price> prices = priceRepository.findByProductIdOrderByPriceAsc(id);
        return toProductResponse(product, enrichPricesWithSavings(prices));
    }

    public List<PriceResponse> getPrices(Long productId) {
        if (!productRepository.existsById(productId))
            throw new CustomExceptions.ProductNotFoundException(productId);
        List<Price> prices = priceRepository.findByProductIdOrderByPriceAsc(productId);
        return enrichPricesWithSavings(prices);
    }

    public PriceHistoryResponse getHistory(Long productId, Integer days, String platform) {
        if (!productRepository.existsById(productId))
            throw new CustomExceptions.ProductNotFoundException(productId);
        int d = days == null || days < 1 ? 14 : Math.min(days, 90);
        Instant since = Instant.now().minus(d, ChronoUnit.DAYS);
        List<PriceHistory> history = platform != null && !platform.isBlank()
                ? priceHistoryRepository.findByProductIdAndPlatformSince(productId, platform, since)
                : priceHistoryRepository.findByProductIdSince(productId, since);
        Map<String, List<PriceHistoryResponse.DataPoint>> byPlatform = history.stream()
                .collect(Collectors.groupingBy(PriceHistory::getPlatform,
                        Collectors.mapping(ph -> new PriceHistoryResponse.DataPoint(ph.getRecordedAt(), ph.getPrice()),
                                Collectors.toList())));
        byPlatform.forEach((k, v) -> v.sort(Comparator.comparing(PriceHistoryResponse.DataPoint::getDate)));
        return PriceHistoryResponse.builder()
                .productId(productId)
                .historyByPlatform(byPlatform)
                .build();
    }

    public RecommendationResponse getRecommendation(Long productId) {
        if (!productRepository.existsById(productId))
            throw new CustomExceptions.ProductNotFoundException(productId);
        return aiRecommendationService.getRecommendation(productId);
    }

    @Transactional
    public void trackProduct(Long productId, String userId) {
        if (!productRepository.existsById(productId))
            throw new CustomExceptions.ProductNotFoundException(productId);
        String uid = userId != null ? userId : "default";
        if (trackedProductRepository.existsByProductIdAndUserId(productId, uid)) return;
        Product product = productRepository.getReferenceById(productId);
        trackedProductRepository.save(TrackedProduct.builder().product(product).userId(uid).build());
    }

    private List<PriceResponse> enrichPricesWithSavings(List<Price> prices) {
        if (prices.isEmpty()) return Collections.emptyList();
        BigDecimal max = prices.stream().map(Price::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        return prices.stream()
                .map(p -> {
                    int savings = max.compareTo(BigDecimal.ZERO) > 0
                            ? max.subtract(p.getPrice()).multiply(BigDecimal.valueOf(100)).divide(max, 0, java.math.RoundingMode.HALF_UP).intValue()
                            : 0;
                    return PriceResponse.builder()
                            .id(p.getId())
                            .platform(p.getPlatform())
                            .price(p.getPrice())
                            .currency(p.getCurrency())
                            .productUrl(p.getProductUrl())
                            .availability(p.getAvailability())
                            .recordedAt(p.getRecordedAt())
                            .percentSavings(savings)
                            .build();
                })
                .sorted(Comparator.comparing(PriceResponse::getPrice))
                .toList();
    }

    private ProductResponse toProductResponse(Product product, List<PriceResponse> priceResponses) {
        PriceResponse best = priceResponses.isEmpty() ? null : priceResponses.get(0);
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .specifications(product.getSpecifications())
                .prices(priceResponses)
                .bestPrice(best)
                .build();
    }

}
