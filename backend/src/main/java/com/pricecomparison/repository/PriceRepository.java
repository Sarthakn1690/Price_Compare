package com.pricecomparison.repository;

import com.pricecomparison.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByProductIdOrderByPriceAsc(Long productId);

    void deleteByProductId(Long productId);
}
