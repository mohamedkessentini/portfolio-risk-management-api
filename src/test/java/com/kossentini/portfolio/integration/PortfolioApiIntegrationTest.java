package com.kossentini.portfolio.integration;

import com.kossentini.portfolio.domain.InstrumentType;
import com.kossentini.portfolio.domain.TransactionType;
import com.kossentini.portfolio.web.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PortfolioApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("portfolio_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Test
    void createsPortfolioBuysInstrumentAndComputesValuation() {
        CreatePortfolioRequest createPortfolio = new CreatePortfolioRequest("Growth Fund", "Mohamed", "EUR");
        ResponseEntity<PortfolioResponse> portfolioResponse = restTemplate.postForEntity(
                baseUrl() + "/portfolios", createPortfolio, PortfolioResponse.class);
        assertThat(portfolioResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long portfolioId = portfolioResponse.getBody().id();

        CreateInstrumentRequest createInstrument = new CreateInstrumentRequest(
                "AAPL", "Apple Inc.", InstrumentType.EQUITY, "USD", new BigDecimal("190.00"));
        ResponseEntity<InstrumentResponse> instrumentResponse = restTemplate.postForEntity(
                baseUrl() + "/instruments", createInstrument, InstrumentResponse.class);
        assertThat(instrumentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long instrumentId = instrumentResponse.getBody().id();

        CreateTransactionRequest buy = new CreateTransactionRequest(
                instrumentId, TransactionType.BUY, new BigDecimal("10"), new BigDecimal("150.00"));
        ResponseEntity<TransactionResponse> transactionResponse = restTemplate.postForEntity(
                baseUrl() + "/portfolios/" + portfolioId + "/transactions", buy, TransactionResponse.class);
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<PortfolioValuationResponse> valuationResponse = restTemplate.getForEntity(
                baseUrl() + "/portfolios/" + portfolioId + "/valuation", PortfolioValuationResponse.class);

        assertThat(valuationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PortfolioValuationResponse valuation = valuationResponse.getBody();
        assertThat(valuation.totalMarketValue()).isEqualByComparingTo("1900.0000");
        assertThat(valuation.totalCostBasis()).isEqualByComparingTo("1500.0000");
        assertThat(valuation.allocationByInstrumentType().get("EQUITY")).isEqualByComparingTo("100.00");
    }

    @Test
    void sellingMoreThanHeldReturnsUnprocessableEntity() {
        CreatePortfolioRequest createPortfolio = new CreatePortfolioRequest("Income Fund", "Mohamed", "EUR");
        Long portfolioId = restTemplate.postForEntity(
                baseUrl() + "/portfolios", createPortfolio, PortfolioResponse.class).getBody().id();

        CreateInstrumentRequest createInstrument = new CreateInstrumentRequest(
                "MSFT", "Microsoft Corp.", InstrumentType.EQUITY, "USD", new BigDecimal("420.00"));
        Long instrumentId = restTemplate.postForEntity(
                baseUrl() + "/instruments", createInstrument, InstrumentResponse.class).getBody().id();

        CreateTransactionRequest sell = new CreateTransactionRequest(
                instrumentId, TransactionType.SELL, new BigDecimal("5"), new BigDecimal("420.00"));
        ResponseEntity<ApiErrorTestView> response = restTemplate.postForEntity(
                baseUrl() + "/portfolios/" + portfolioId + "/transactions", sell, ApiErrorTestView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void creatingPortfolioWithBlankNameReturnsBadRequest() {
        CreatePortfolioRequest invalid = new CreatePortfolioRequest("", "Mohamed", "EUR");
        ResponseEntity<ApiErrorTestView> response = restTemplate.postForEntity(
                baseUrl() + "/portfolios", invalid, ApiErrorTestView.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private record ApiErrorTestView(String error, String message) {
    }
}
