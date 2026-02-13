package com.pricecomparison.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public abstract class BaseScraper implements Scraper {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${scraper.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36}")
    protected String userAgent;

    @Value("${scraper.timeout:10000}")
    protected int timeout;

    @Value("${scraper.retry-attempts:3}")
    protected int retryAttempts;

    protected Document fetchDocument(String url) throws Exception {
        Exception lastException = null;
        for (int i = 0; i < retryAttempts; i++) {
            try {
                randomDelay();
                return Jsoup.connect(url)
                        .userAgent(userAgent)
                        .timeout(timeout)
                        .followRedirects(true)
                        .get();
            } catch (Exception e) {
                lastException = e;
                log.warn("Scrape attempt {} failed for {}: {}", i + 1, url, e.getMessage());
            }
        }
        throw lastException != null ? lastException : new RuntimeException("Scraping failed");
    }

    protected void randomDelay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    protected BigDecimal parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        String cleaned = text.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return null;
        try {
            return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Map<String, String> emptySpecs() {
        return new HashMap<>();
    }
}
