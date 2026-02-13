package com.pricecomparison.service;

import com.pricecomparison.dto.RecommendationResponse;
import com.pricecomparison.model.PriceHistory;
import com.pricecomparison.repository.PriceHistoryRepository;
import com.pricecomparison.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceRepository priceRepository;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    public RecommendationResponse getRecommendation(Long productId) {
        List<PriceHistory> history = priceHistoryRepository.findByProductIdSince(
                productId, Instant.now().minus(14, ChronoUnit.DAYS));
        var currentPrices = priceRepository.findByProductIdOrderByPriceAsc(productId);
        if (currentPrices.isEmpty()) {
            return defaultRecommendation("No current price data");
        }
        BigDecimal lowestCurrent = currentPrices.get(0).getPrice();
        BigDecimal avgHistorical = averagePrice(history);
        boolean hasHistory = !history.isEmpty();
        if (!hasHistory || avgHistorical == null) {
            return RecommendationResponse.builder()
                    .recommendation(RecommendationResponse.RecommendationType.BUY_NOW)
                    .confidenceScore(50)
                    .explanation("Insufficient history. Current best price: ₹" + lowestCurrent)
                    .predictedTrend("Unknown - need more data")
                    .build();
        }
        int trend = lowestCurrent.compareTo(avgHistorical);
        double percentDiff = avgHistorical.doubleValue() > 0
                ? (lowestCurrent.subtract(avgHistorical).doubleValue() / avgHistorical.doubleValue()) * 100
                : 0;
        RecommendationResponse.RecommendationType type;
        String explanation;
        int confidence;
        if (trend <= 0 && percentDiff <= -5) {
            type = RecommendationResponse.RecommendationType.BUY_NOW;
            explanation = String.format("Price is %.1f%% below 14-day average. Good time to buy.", -percentDiff);
            confidence = Math.min(90, 70 + (int) (-percentDiff));
        } else if (trend > 0 && percentDiff > 5) {
            type = RecommendationResponse.RecommendationType.PRICE_INCREASING;
            explanation = String.format("Price is %.1f%% above recent average. Consider waiting.", percentDiff);
            confidence = 75;
        } else {
            type = RecommendationResponse.RecommendationType.WAIT_FOR_BETTER_PRICE;
            explanation = "Price is near average. Waiting may yield a better deal.";
            confidence = 65;
        }
        return RecommendationResponse.builder()
                .recommendation(type)
                .confidenceScore(Math.min(95, confidence))
                .explanation(explanation)
                .predictedTrend(trend <= 0 ? "Stable or decreasing" : "Increasing")
                .build();
    }

    private BigDecimal averagePrice(List<PriceHistory> history) {
        if (history.isEmpty()) return null;
        BigDecimal sum = history.stream()
                .map(PriceHistory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);
    }

    private RecommendationResponse defaultRecommendation(String reason) {
        return RecommendationResponse.builder()
                .recommendation(RecommendationResponse.RecommendationType.BUY_NOW)
                .confidenceScore(50)
                .explanation(reason)
                .predictedTrend("Unknown")
                .build();
    }
}
