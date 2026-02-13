package com.pricecomparison.service;

import com.pricecomparison.exception.CustomExceptions;
import com.pricecomparison.scraper.Scraper;
import com.pricecomparison.scraper.ScraperResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScraperService {

    private final List<Scraper> scrapers;

    public Optional<Scraper> getScraperForUrl(String url) {
        return scrapers.stream().filter(s -> s.canHandle(url)).findFirst();
    }

    /**
     * Expose all configured scrapers for cross-platform searches.
     */
    public List<Scraper> getAllScrapers() {
        return scrapers;
    }

    public ScraperResult scrapeUrl(String url) {
        Scraper scraper = getScraperForUrl(url)
                .orElseThrow(() -> new CustomExceptions.InvalidUrlException("Unsupported platform: " + url));
        try {
            return scraper.scrape(url);
        } catch (Exception e) {
            log.error("Scraping failed for {}: {}", url, e.getMessage());
            throw new CustomExceptions.ScrapingFailedException(e.getMessage(), e);
        }
    }

    public CompletableFuture<ScraperResult> scrapeUrlAsync(String url) {
        return CompletableFuture.supplyAsync(() -> scrapeUrl(url));
    }
}
