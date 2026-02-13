package com.pricecomparison.controller;

import com.pricecomparison.dto.*;
import com.pricecomparison.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/search")
    public ResponseEntity<ProductResponse> search(@Valid @RequestBody ProductSearchRequest request) {
        return ResponseEntity.ok(productService.searchByUrl(request.getUrl().trim()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<java.util.List<PriceResponse>> getPrices(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getPrices(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<PriceHistoryResponse> getHistory(
            @PathVariable Long id,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String platform) {
        return ResponseEntity.ok(productService.getHistory(id, days, platform));
    }

    @GetMapping("/{id}/recommendation")
    public ResponseEntity<RecommendationResponse> getRecommendation(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getRecommendation(id));
    }

    @PostMapping("/{id}/track")
    public ResponseEntity<Map<String, String>> track(
            @PathVariable Long id,
            @RequestParam(required = false) String userId) {
        productService.trackProduct(id, userId);
        return ResponseEntity.ok(Map.of("message", "Product added to watchlist"));
    }
}
