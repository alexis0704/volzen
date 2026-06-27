package com.app.venus.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.app.venus.shared.web.ProductApiError;

class GlobalExceptionHandlerProductTests {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFoundToProductError() {
        var response = handler.handleProductApiException(
                new NotFoundException("Provider not found."),
                productRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isEqualTo(new ProductApiError("RESOURCE_NOT_FOUND", "Provider not found."));
    }

    @Test
    void mapsConflictToProductError() {
        var response = handler.handleProductApiException(
                new ConflictException("SLOT_UNAVAILABLE", "The requested time slot is no longer available."),
                productRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isEqualTo(new ProductApiError(
                "SLOT_UNAVAILABLE",
                "The requested time slot is no longer available."));
    }

    @Test
    void mapsUnprocessableEntityToProductError() {
        var response = handler.handleProductApiException(
                new UnprocessableEntityException("End time must be after start time."),
                productRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody()).isEqualTo(new ProductApiError(
                "UNPROCESSABLE_ENTITY",
                "End time must be after start time."));
    }

    private MockHttpServletRequest productRequest() {
        return new MockHttpServletRequest("GET", "/api/v1/test");
    }
}
