package com.kossentini.portfolio.repository;

import com.kossentini.portfolio.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query("select p from Position p where p.portfolio.id = :portfolioId and p.instrument.id = :instrumentId")
    Optional<Position> findByPortfolioIdAndInstrumentId(Long portfolioId, Long instrumentId);

    @Query("select p from Position p join fetch p.instrument where p.portfolio.id = :portfolioId")
    List<Position> findAllByPortfolioIdWithInstrument(Long portfolioId);
}
