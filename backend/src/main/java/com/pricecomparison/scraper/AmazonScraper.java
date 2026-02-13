package com.pricecomparison.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AmazonScraper extends BaseScraper {

    private static final String BASE_DOMAIN = "https://www.amazon.in";

    @Override
    public String getPlatformName() {
        return "amazon";
    }

    @Override
    public boolean canHandle(String url) {
        return url != null && url.contains("amazon.");
    }

    @Override
    public boolean supportsSearch() {
        return true;
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

    @Override
    public ScraperResult searchByNameAndBrand(String name, String brand) throws Exception {
        String query = buildQuery(name, brand);
        String searchUrl = BASE_DOMAIN + "/s?k=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        Document doc = fetchDocument(searchUrl);

        // Amazon search results: pick first organic result
        Element first = doc.selectFirst("div.s-main-slot div[data-component-type='s-search-result'] h2 a");
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
