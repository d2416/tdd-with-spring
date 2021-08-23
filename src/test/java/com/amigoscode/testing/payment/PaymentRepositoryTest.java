package com.amigoscode.testing.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        properties = {"spring.jpa.properties.javax.persistence.validation.mode=none"}
)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository underTest;

    @Test
    void itShouldInsertPayment() {
        // Given
        long paymentId = 1L;
        Payment payment = new Payment(
                paymentId, UUID.randomUUID(), new BigDecimal(10), Currency.EUR, "Card123", "Donation");

        // When
        underTest.save(payment);

        // Then
        Optional<Payment> byId = underTest.findById(paymentId);
        assertThat(byId).hasValueSatisfying(p -> {
            assertThat(p).isEqualTo(payment);
        });
    }
}