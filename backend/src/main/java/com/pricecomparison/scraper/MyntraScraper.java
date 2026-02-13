package com.pricecomparison.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class MyntraScraper extends BaseScraper {

    private static final String BASE_DOMAIN = "https://www.myntra.com";

    @Override
    public String getPlatformName() {
        return "myntra";
    }

    @Override
    public boolean canHandle(String url) {
        return url != null && url.contains("myntra.com");
    }

    @Override
    public boolean supportsSearch() {
        return true;
    }

    @Override
    public ScraperResult scrape(String url) throws Exception {
        Document doc = fetchDocument(url);
        String name = extractText(doc, ".pdp-title", ".pdp-name", "h1.pdp-title");
        String brand = extractText(doc, ".pdp-brand", ".pdp-product-brand");
        BigDecimal price = parsePrice(extractText(doc, ".pdp-price", ".pdp-discount-container .pdp-price"));
        String imageUrl = extractAttr(doc, ".image-grid-image", "src");
        if (imageUrl == null) imageUrl = extractAttr(doc, "img.pdp-image", "src");

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

        // Myntra search results: product cards links
        Element first = doc.selectFirst("li.product-base a"); // typical product tile
        if (first == null) {
            first = doc.selectFirst("a.product-base"); // fallback
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
