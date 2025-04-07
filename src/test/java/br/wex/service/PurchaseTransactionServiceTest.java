package br.wex.service;

import br.wex.dto.CreatePurchaseTransactionRequest;
import br.wex.dto.CurrencyConversionResult;
import br.wex.dto.PurchaseTransactionResponse;
import br.wex.exception.CurrencyConversionException;
import br.wex.exception.TransactionNotFoundException;
import br.wex.model.PurchaseTransaction;
import br.wex.repository.PurchaseTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseTransactionServiceTest {

    @Mock
    private PurchaseTransactionRepository purchaseTransactionRepository;

    @Mock
    private CurrencyExchangeService currencyExchangeService;

    @InjectMocks
    private PurchaseTransactionService purchaseTransactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTransactionShouldSaveTransaction() {
        CreatePurchaseTransactionRequest request = new CreatePurchaseTransactionRequest(
                "Test Purchase",
                LocalDate.now(),
                new BigDecimal("10.00")
        );

        PurchaseTransaction expectedTransaction = PurchaseTransaction.builder()
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .purchaseAmount(new BigDecimal("10.00"))
                .build();

        when(purchaseTransactionRepository.save(any())).thenReturn(expectedTransaction);

        PurchaseTransaction saved = purchaseTransactionService.createTransaction(request);

        assertEquals("Test Purchase", saved.getDescription());
        assertEquals(new BigDecimal("10.00"), saved.getPurchaseAmount());
        verify(purchaseTransactionRepository, times(1)).save(any());
    }

    @Test
    void formatPurchaseAmountShouldRoundCorrectly() {
        BigDecimal result = purchaseTransactionService.formatPurchaseAmount(new BigDecimal("10.567"));
        assertEquals(new BigDecimal("10.57"), result);
    }

    @Test
    void formatPurchaseAmountShouldThrowForNullOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> purchaseTransactionService.formatPurchaseAmount(null));
        assertThrows(IllegalArgumentException.class, () -> purchaseTransactionService.formatPurchaseAmount(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> purchaseTransactionService.formatPurchaseAmount(new BigDecimal("-1")));
    }

    @Test
    void getTransactionWithConversion_shouldReturnConvertedResponse() throws TransactionNotFoundException, CurrencyConversionException {
        Long id = 1L;
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .id(id)
                .description("Test")
                .transactionDate(LocalDate.of(2023, 10, 1))
                .purchaseAmount(new BigDecimal("100.00"))
                .build();

        when(purchaseTransactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(currencyExchangeService.convertUSDTo("Canada-Dollar", new BigDecimal("100.00"), LocalDate.of(2023, 10, 1)))
                .thenReturn(new CurrencyConversionResult(new BigDecimal("0.9"), new BigDecimal("90.00"), LocalDate.now()));

        PurchaseTransactionResponse response = purchaseTransactionService.getTransactionWithConversion("Canada-Dollar", id);

        assertEquals(new BigDecimal("90.00"), response.convertedAmount());
        assertEquals(new BigDecimal("0.9"), response.exchangeRate());
    }

    @Test
    void getTransactionWithConversionShouldThrowWhenTransactionNotFound() {
        when(purchaseTransactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> purchaseTransactionService.getTransactionWithConversion("Canada-Dollar", 1L));
    }
}
