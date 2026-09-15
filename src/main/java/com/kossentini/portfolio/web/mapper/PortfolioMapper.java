package com.kossentini.portfolio.web.mapper;

import com.kossentini.portfolio.domain.Portfolio;
import com.kossentini.portfolio.web.dto.PortfolioResponse;
import org.springframework.stereotype.Component;

@Component
public class PortfolioMapper {

    public PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getOwnerName(),
                portfolio.getBaseCurrency(),
                portfolio.getCreatedAt()
        );
    }
}
