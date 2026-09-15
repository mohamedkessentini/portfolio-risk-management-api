package com.kossentini.portfolio.web.mapper;

import com.kossentini.portfolio.domain.Transaction;
import com.kossentini.portfolio.web.dto.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getPortfolio().getId(),
                transaction.getInstrument().getId(),
                transaction.getInstrument().getSymbol(),
                transaction.getType(),
                transaction.getQuantity(),
                transaction.getPrice(),
                transaction.getTransactionDate()
        );
    }
}
