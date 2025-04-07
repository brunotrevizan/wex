package br.wex.dto;

import br.wex.model.PurchaseTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseTransactionResponse(
        Long id,
        String description,
        LocalDate transactionDate,
        BigDecimal originalAmount,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount
) {
    public static PurchaseTransactionResponse from(PurchaseTransaction transaction, BigDecimal rate, BigDecimal convertedAmount) {
        return new PurchaseTransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPurchaseAmount(),
                rate,
                convertedAmount
        );
    }
}
