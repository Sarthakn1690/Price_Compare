package com.pricecomparison.scraper;

public interface Scraper {

    String getPlatformName();

    /**
     * Whether this scraper can handle a concrete product URL for this platform.
     */
    boolean canHandle(String url);

    /**
     * Scrape a concrete product URL and return structured data.
     */
    ScraperResult scrape(String url) throws Exception;

    /**
     * Whether this scraper supports searching for an equivalent product on this platform
     * using a name/brand combination.
     *
     * Default is {@code false} – override when implemented.
     */
    default boolean supportsSearch() {
        return false;
    }

    /**
     * Best-effort search for an equivalent product on this platform using the given
     * name and brand. Implementations will typically:
     *
     *  1. Build a platform-specific search URL from the name/brand
     *  2. Parse the search results page to find the most relevant product link
     *  3. Delegate to {@link #scrape(String)} for the product detail page
     *
     * May return {@code null} if nothing relevant is found.
     */
    default ScraperResult searchByNameAndBrand(String name, String brand) throws Exception {
        throw new UnsupportedOperationException("Search not implemented for platform " + getPlatformName());
    }
}
