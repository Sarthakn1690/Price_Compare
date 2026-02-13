package com.pricecomparison.service;

import com.pricecomparison.model.Price;
import com.pricecomparison.model.PriceHistory;
import com.pricecomparison.model.Product;
import com.pricecomparison.model.TrackedProduct;
import com.pricecomparison.repository.PriceHistoryRepository;
import com.pricecomparison.repository.PriceRepository;
import com.pricecomparison.repository.TrackedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceTrackingService {

    private static final BigDecimal PRICE_CHANGE_THRESHOLD = new BigDecimal("0.05"); // 5%
    private static final int HISTORY_RETENTION_DAYS = 90;

    private final TrackedProductRepository trackedProductRepository;
    private final PriceRepository priceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ScraperService scraperService;

    @Scheduled(cron = "${scheduler.price-tracking-cron:0 0 */6 * * *}")
    @Transactional
    public void runScheduledPriceUpdate() {
        List<TrackedProduct> tracked = trackedProductRepository.findAll();
        log.info("Running price tracking for {} products", tracked.size());
        for (TrackedProduct tp : tracked) {
            try {
                updatePricesForProduct(tp.getProduct());
            } catch (Exception e) {
                log.warn("Failed to update prices for product {}: {}", tp.getProduct().getId(), e.getMessage());
            }
        }
        cleanupOldHistory();
    }

    public void updatePricesForProduct(Product product) {
        List<Price> currentPrices = priceRepository.findByProductIdOrderByPriceAsc(product.getId());
        for (Price p : currentPrices) {
            if (p.getProductUrl() == null || p.getProductUrl().isBlank()) continue;
            try {
                var result = scraperService.scrapeUrl(p.getProductUrl());
                if (result.getPrice() != null && result.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal oldPrice = p.getPrice();
                    if (oldPrice != null && priceChangeSignificant(oldPrice, result.getPrice())) {
                        priceHistoryRepository.save(PriceHistory.builder()
                                .product(product)
                                .platform(p.getPlatform())
                                .price(result.getPrice())
                                .build());
                    }
                    p.setPrice(result.getPrice());
                    p.setAvailability(result.isAvailability());
                    priceRepository.save(p);
                }
            } catch (Exception e) {
                log.debug("Could not update price for {} on {}: {}", product.getId(), p.getPlatform(), e.getMessage());
            }
        }
    }

    private boolean priceChangeSignificant(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice.compareTo(BigDecimal.ZERO) == 0) return true;
        BigDecimal diff = newPrice.subtract(oldPrice).abs();
        BigDecimal threshold = oldPrice.multiply(PRICE_CHANGE_THRESHOLD);
        return diff.compareTo(threshold) >= 0;
    }

    @Transactional
    public void cleanupOldHistory() {
        Instant cutoff = Instant.now().minus(HISTORY_RETENTION_DAYS, ChronoUnit.DAYS);
        var productIds = priceHistoryRepository.findAll().stream()
                .map(ph -> ph.getProduct().getId())
                .distinct()
                .toList();
        for (Long productId : productIds) {
            priceHistoryRepository.deleteByProductIdAndRecordedAtBefore(productId, cutoff);
        }
        log.debug("Cleaned up price history older than {} days", HISTORY_RETENTION_DAYS);
    }
}
