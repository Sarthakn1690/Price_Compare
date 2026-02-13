package com.pricecomparison.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MyntraScraper extends BaseScraper {

    @Override
    public String getPlatformName() {
        return "myntra";
    }

    @Override
    public boolean canHandle(String url) {
        return url != null && url.contains("myntra.com");
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
