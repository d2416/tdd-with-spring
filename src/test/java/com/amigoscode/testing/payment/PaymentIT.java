package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRegistrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void itShouldCreatePaymentSuccessfuly() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Juan", "00000000");

        ResultActions resultActions = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectToJson(new CustomerRegistrationRequest(customer)))));

        // When

        // Then
        resultActions.andExpect(status().isOk());
    }

    private String objectToJson(Object o) {
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to JSON");
            return null;
        }
    }
}
