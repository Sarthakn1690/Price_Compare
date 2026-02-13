package com.pricecomparison.scraper;

public interface Scraper {

    String getPlatformName();

    boolean canHandle(String url);

    ScraperResult scrape(String url) throws Exception;
}
