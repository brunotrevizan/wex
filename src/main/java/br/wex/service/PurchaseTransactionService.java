package br.wex.service;

import br.wex.dto.CreatePurchaseTransactionRequest;
import br.wex.dto.CurrencyConversionResult;
import br.wex.dto.PurchaseTransactionResponse;
import br.wex.exception.CurrencyConversionException;
import br.wex.exception.TransactionNotFoundException;
import br.wex.model.PurchaseTransaction;
import br.wex.repository.PurchaseTransactionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PurchaseTransactionService {

    private PurchaseTransactionRepository purchaseTransactionRepository;
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    public PurchaseTransactionService(PurchaseTransactionRepository purchaseTransactionRepository, CurrencyExchangeService currencyExchangeService) {
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.currencyExchangeService = currencyExchangeService;
    }

    public PurchaseTransaction createTransaction(@Valid CreatePurchaseTransactionRequest request) {
        validateDescription(request.getDescription());
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .purchaseAmount(formatPurchaseAmount(request.getAmount()))
                .build();
        return purchaseTransactionRepository.save(transaction);
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 50) {
            throw new IllegalArgumentException("Description can't be longer than 50 characters.");
        }
    }

    public BigDecimal formatPurchaseAmount(BigDecimal purchaseAmount) {
        if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase value must be positive.");
        }
        return purchaseAmount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public PurchaseTransactionResponse getTransactionWithConversion(String targetCurrency, Long id) throws TransactionNotFoundException, CurrencyConversionException {
        PurchaseTransaction purchaseTransaction = purchaseTransactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found."));

        CurrencyConversionResult currencyConversionResult = currencyExchangeService.convertUSDTo(targetCurrency, purchaseTransaction.getPurchaseAmount(), purchaseTransaction.getTransactionDate());
        return PurchaseTransactionResponse.from(purchaseTransaction, currencyConversionResult.exchangeRate(), currencyConversionResult.convertedAmount());
    }
}
