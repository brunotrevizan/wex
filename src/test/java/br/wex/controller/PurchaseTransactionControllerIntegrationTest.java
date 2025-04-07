package br.wex.controller;

import br.wex.WexApplication;
import br.wex.model.PurchaseTransaction;
import br.wex.repository.PurchaseTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = WexApplication.class)
@AutoConfigureMockMvc
class PurchaseTransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseTransactionRepository transactionRepository;

    private Long transactionId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Test Purchase");
        transaction.setPurchaseAmount(new BigDecimal("100.00"));
        transaction.setTransactionDate(LocalDate.of(2024, 11, 15));

        PurchaseTransaction saved = transactionRepository.save(transaction);
        transactionId = saved.getId();
    }

    @Test
    void shouldReturnConvertedTransaction() throws Exception {
        mockMvc.perform(get("/transactions/" + transactionId)
                .param("currency", "Canada-Dollar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(transactionId))
            .andExpect(jsonPath("$.description").value("Test Purchase"))
            .andExpect(jsonPath("$.originalAmount").value(100.00))
            .andExpect(jsonPath("$.convertedAmount").exists())
            .andExpect(jsonPath("$.exchangeRate").exists());
    }
}
