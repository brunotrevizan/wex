package br.wex.controller;

import br.wex.dto.CreatePurchaseTransactionRequest;
import br.wex.dto.PurchaseTransactionResponse;
import br.wex.exception.CurrencyConversionException;
import br.wex.exception.TransactionNotFoundException;
import br.wex.model.PurchaseTransaction;
import br.wex.service.PurchaseTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Endpoints related to transactions")
public class PurchaseTransactionController {

    private PurchaseTransactionService purchaseTransactionService;

    @Autowired
    public PurchaseTransactionController(PurchaseTransactionService purchaseTransactionService) {
        this.purchaseTransactionService = purchaseTransactionService;
    }

    @PostMapping
    @Operation(summary = "Creating new Transaction", description = "Creates a new transaction and saves it in the database")
    public ResponseEntity<PurchaseTransaction> createTransaction(@Valid @RequestBody CreatePurchaseTransactionRequest request) {
        PurchaseTransaction saved = purchaseTransactionService.createTransaction(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a Transaction", description = "Retrieves a Transaction with the given id from the database with the value converted to the given currency")
    public ResponseEntity<PurchaseTransactionResponse> getTransactionWithConversion(
            @PathVariable Long id,
            @RequestParam(name = "currency", required = true) String targetCurrency) throws CurrencyConversionException, TransactionNotFoundException {
        return ResponseEntity.ok(purchaseTransactionService.getTransactionWithConversion(targetCurrency, id));
    }

}
