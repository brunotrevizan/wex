package br.wex.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyConversionResult(BigDecimal exchangeRate, BigDecimal convertedAmount, LocalDate rateDate) {}
