package br.wex.service;

import br.wex.dto.CurrencyConversionResult;
import br.wex.exception.CurrencyConversionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyExchangeService {

    @Value("${exchange.api}")
    private String API_URL;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CurrencyConversionResult convertUSDTo(String currency, BigDecimal amountUSD, LocalDate transactionDate) throws CurrencyConversionException {
        var data = getResponseFromAPI(currency, transactionDate);
        validateResults(data);
        return processResult(amountUSD, data);
    }

    private CurrencyConversionResult processResult(BigDecimal amountUSD, List<Map<String, String>> data) {
        Map<String, String> lastRecord = data.get(data.size() - 1);
        String rateStr = lastRecord.get("exchange_rate");
        String rateDateStr = lastRecord.get("record_date");

        BigDecimal exchangeRate = new BigDecimal(rateStr);
        LocalDate rateDate = LocalDate.parse(rateDateStr);

        BigDecimal convertedAmount = amountUSD.multiply(exchangeRate).setScale(2, RoundingMode.HALF_EVEN);

        return new CurrencyConversionResult(exchangeRate, convertedAmount, rateDate);
    }

    private void validateResults(List<Map<String, String>> data) throws CurrencyConversionException {
        if (data == null || data.isEmpty()) {
            throw new CurrencyConversionException("No exchange rate available within 6 months before the purchase date.");
        }
    }

    private List<Map<String, String>> getResponseFromAPI(String currency, LocalDate transactionDate) {
        LocalDate sixMonthsAgo = transactionDate.minusMonths(6);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_URL);
        urlBuilder.append(String.format("?fields=exchange_rate,record_date"));
        urlBuilder.append(String.format("&filter=country_currency_desc:in:(%s),record_date:lte:%s,record_date:gt:%s&sort=record_date",
                currency,
                transactionDate.format(DATE_FORMATTER),
                sixMonthsAgo.format(DATE_FORMATTER)));

        Map<?, ?> response = restTemplate.getForObject(urlBuilder.toString(), Map.class);
        var data = (List<Map<String, String>>) response.get("data");
        return data;
    }

}
