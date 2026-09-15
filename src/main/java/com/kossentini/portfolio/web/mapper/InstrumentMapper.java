package com.kossentini.portfolio.web.mapper;

import com.kossentini.portfolio.domain.Instrument;
import com.kossentini.portfolio.web.dto.InstrumentResponse;
import org.springframework.stereotype.Component;

@Component
public class InstrumentMapper {

    public InstrumentResponse toResponse(Instrument instrument) {
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getSymbol(),
                instrument.getName(),
                instrument.getType(),
                instrument.getCurrency(),
                instrument.getCurrentPrice(),
                instrument.getPriceUpdatedAt()
        );
    }
}
