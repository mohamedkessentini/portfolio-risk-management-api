package com.kossentini.portfolio.repository;

import com.kossentini.portfolio.domain.Instrument;
import com.kossentini.portfolio.domain.InstrumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbolIgnoreCase(String symbol);

    boolean existsBySymbolIgnoreCase(String symbol);

    @Query("""
            select i from Instrument i
            where (:type is null or i.type = :type)
            and (:search is null
                 or lower(i.symbol) like lower(concat('%', :search, '%'))
                 or lower(i.name) like lower(concat('%', :search, '%')))
            """)
    Page<Instrument> search(InstrumentType type, String search, Pageable pageable);
}
