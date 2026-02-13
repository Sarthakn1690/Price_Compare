package com.pricecomparison.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmazonScraper extends BaseScraper {

    @Override
    public String getPlatformName() {
        return "amazon";
    }

    @Override
    public boolean canHandle(String url) {
        return url != null && url.contains("amazon.");
    }

    @Override
    public ScraperResult scrape(String url) throws Exception {
        Document doc = fetchDocument(url);
        String name = extractText(doc, "#productTitle", ".a-size-large");
        String brand = extractText(doc, "#bylineInfo", ".po-brand .po-break-word");
        BigDecimal price = parsePrice(extractText(doc, ".a-price-whole", ".a-offscreen", "#priceblock_ourprice", "#priceblock_dealprice"));
        String imageUrl = extractAttr(doc, "#landingImage", "src");
        if (imageUrl == null) imageUrl = extractAttr(doc, "img#imgBlkFront", "src");

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
