package com.pricecomparison.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class FlipkartScraper extends BaseScraper {

    private static final String BASE_DOMAIN = "https://www.flipkart.com";

    @Override
    public String getPlatformName() {
        return "flipkart";
    }

    @Override
    public boolean canHandle(String url) {
        return url != null && url.contains("flipkart.com");
    }

    @Override
    public boolean supportsSearch() {
        return true;
    }

    @Override
    public ScraperResult scrape(String url) throws Exception {
        Document doc = fetchDocument(url);
        String name = extractText(doc, ".B_NuCI", ".yhB1nd", "span.VU-ZEz");
        String brand = extractText(doc, ".G6XhRU");
        BigDecimal price = parsePrice(extractText(doc, "._30jeq3._16Jk6d", "._25b18c ._30jeq3", ".CxhGGd"));
        String imageUrl = extractAttr(doc, "._396QI4", "src");
        if (imageUrl == null) imageUrl = extractAttr(doc, "img[class*='_396QI']", "src");

        return ScraperResult.builder()
                .name(name != null ? name.trim() : "Unknown Product")
                .brand(brand)
                .category(null)
                .imageUrl(imageUrl)
                .specifications(emptySpecs())
                .price(price != null ? price : BigDecimal.ZERO)
                .productUrl(url)
                .availability(price != null)
                .platform(getPlatformName())
                .build();
    }

    @Override
    public ScraperResult searchByNameAndBrand(String name, String brand) throws Exception {
        String query = buildQuery(name, brand);
        String searchUrl = BASE_DOMAIN + "/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        Document doc = fetchDocument(searchUrl);

        // Flipkart search results: pick first grid/list card
        Element first = doc.selectFirst("a._1fQZEK"); // common for main cards
        if (first == null) {
            first = doc.selectFirst("a.s1Q9rs"); // fallback selector
        }
        if (first == null) {
            return null;
        }
        String href = first.attr("href");
        if (!href.startsWith("http")) {
            href = BASE_DOMAIN + href;
        }
        return scrape(href);
    }

    private String buildQuery(String name, String brand) {
        StringBuilder sb = new StringBuilder();
        if (brand != null && !brand.isBlank()) {
            sb.append(brand).append(" ");
        }
        if (name != null && !name.isBlank()) {
            sb.append(name);
        }
        return sb.toString().trim();
    }

    private String extractText(Document doc, String... selectors) {
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null) {
                String text = el.text();
                if (text != null && !text.isBlank()) return text;
            }
        }
        return null;
    }

    private String extractAttr(Document doc, String selector, String attr) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.attr(attr) : null;
    }
}
