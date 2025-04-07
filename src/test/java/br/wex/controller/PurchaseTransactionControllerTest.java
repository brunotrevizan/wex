package br.wex.controller;

import br.wex.dto.CreatePurchaseTransactionRequest;
import br.wex.dto.PurchaseTransactionResponse;
import br.wex.model.PurchaseTransaction;
import br.wex.service.PurchaseTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PurchaseTransactionController.class)
class PurchaseTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseTransactionService purchaseTransactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTransaction() throws Exception {
        CreatePurchaseTransactionRequest request = CreatePurchaseTransactionRequest.builder()
                .description("Books")
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .build();

        PurchaseTransaction mockTransaction = new PurchaseTransaction();
        mockTransaction.setId(1L);
        mockTransaction.setDescription(request.getDescription());
        mockTransaction.setPurchaseAmount(request.getAmount());
        mockTransaction.setTransactionDate(request.getTransactionDate());

        Mockito.when(purchaseTransactionService.createTransaction(Mockito.any()))
                .thenReturn(mockTransaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Books"));
    }

    @Test
    void testGetTransactionWithConversion() throws Exception {
        Long transactionId = 1L;
        String currency = "EUR";

        PurchaseTransactionResponse response = new PurchaseTransactionResponse(
                transactionId,
                "Compra de livros",
                LocalDate.of(2024, 3, 15),
                new BigDecimal("100.00"),
                new BigDecimal("0.95"),
                new BigDecimal("95.00")
        );

        Mockito.when(purchaseTransactionService.getTransactionWithConversion(currency, transactionId))
                .thenReturn(response);

        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .param("currency", currency))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Compra de livros"))
                .andExpect(jsonPath("$.convertedAmount").value(95.00));
    }
}
