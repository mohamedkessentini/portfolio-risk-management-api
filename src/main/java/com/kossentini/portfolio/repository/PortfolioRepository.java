package com.kossentini.portfolio.repository;

import com.kossentini.portfolio.domain.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Page<Portfolio> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
