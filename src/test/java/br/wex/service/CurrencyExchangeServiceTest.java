package br.wex.service;

import br.wex.dto.CurrencyConversionResult;
import br.wex.exception.CurrencyConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyExchangeServiceTest {

    @InjectMocks
    private CurrencyExchangeService currencyExchangeService;

    @Mock
    private RestTemplate restTemplate;

    private final String mockApiUrl = "https://api.mock.com";

    private final String currency = "Canada-Dollar";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currencyExchangeService = new CurrencyExchangeService();
        ReflectionTestUtils.setField(currencyExchangeService, "API_URL", mockApiUrl);
        ReflectionTestUtils.setField(currencyExchangeService, "restTemplate", restTemplate);
    }

    @Test
    void convertUSDToShouldReturnConvertedResult() throws CurrencyConversionException {
        BigDecimal amountUSD = new BigDecimal("100.00");
        LocalDate transactionDate = LocalDate.of(2024, 12, 1);

        List<Map<String, String>> mockData = List.of(
                Map.of("exchange_rate", "0.85", "record_date", "2024-11-01")
        );
        Map<String, Object> mockResponse = Map.of("data", mockData);

        String expectedUrlFragment = "?fields=exchange_rate,record_date";
        when(restTemplate.getForObject(contains(expectedUrlFragment), eq(Map.class))).thenReturn(mockResponse);

        CurrencyConversionResult result = currencyExchangeService.convertUSDTo(currency, amountUSD, transactionDate);

        assertEquals(new BigDecimal("0.85"), result.exchangeRate());
        assertEquals(new BigDecimal("85.00"), result.convertedAmount());
        assertEquals(LocalDate.of(2024, 11, 1), result.rateDate());
    }

    @Test
    void convertUSDToShouldThrowExceptionWhenDataIsEmpty() {
        BigDecimal amountUSD = new BigDecimal("100.00");
        LocalDate transactionDate = LocalDate.of(2024, 12, 1);

        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        CurrencyConversionException ex = assertThrows(
                CurrencyConversionException.class,
                () -> currencyExchangeService.convertUSDTo(currency, amountUSD, transactionDate)
        );
        assertEquals("No exchange rate available within 6 months before the purchase date.", ex.getMessage());
    }

    @Test
    void processResultShouldCalculateCorrectly() {
        List<Map<String, String>> mockData = List.of(
                Map.of("exchange_rate", "1.23", "record_date", "2025-01-01")
        );
        BigDecimal amount = new BigDecimal("200");

        CurrencyConversionResult result = ReflectionTestUtils.invokeMethod(
                CurrencyExchangeService.class,
                "processResult",
                amount,
                mockData
        );

        assertEquals(new BigDecimal("1.23"), result.exchangeRate());
        assertEquals(new BigDecimal("246.00"), result.convertedAmount());
    }

}
