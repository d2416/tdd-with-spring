package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class PaymentServiceTest {

    private PaymentService underTest;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CardPaymentCharger cardPaymentCharger;

    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new PaymentService(paymentRepository, customerRepository, cardPaymentCharger);

    }

    @Test
    void itShouldChargeCardSuccessfully() {
        // Given
        UUID customerId = UUID.randomUUID();
        // ... customer exists
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... payment request
        BigDecimal amount = new BigDecimal(10);
        Payment payment = new Payment(null, null, amount, Currency.EUR, "source", "description");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        CardPaymentCharge paymentCharge = new CardPaymentCharge(true);

        // ... Card is charged successfully
        given(cardPaymentCharger
                .chargeCard(payment.getSource(), payment.getAmount(), payment.getCurrency(), payment.getDescription()))
                .willReturn(paymentCharge);

        // When
        underTest.chargeCard(customerId, paymentRequest);

        // Then
        ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);

        then(paymentRepository).should().save(paymentArgumentCaptor.capture());
        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();

        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);
        assertThat(paymentArgumentCaptorValue).isEqualTo(payment);
    }

    @Test
    void itShouldThrowWhenCardIsNotCharged() {
        // Given
        UUID customerId = UUID.randomUUID();
        Payment payment = new Payment(null, null, new BigDecimal(100), Currency.EUR, "source", "description");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));
        given(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription())).willReturn(new CardPaymentCharge(false));

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .hasMessage("The card was not debited.")
                .isInstanceOf(IllegalStateException.class);
        then(paymentRepository).should(never()).save(any());
    }

    @Test
    void itShouldNotChargeAndThrowWhenCurrencyIsNotAccepted() {
        // Given
        UUID customerId = UUID.randomUUID();
        Payment payment = new Payment(null, null, new BigDecimal(100),
                Currency.COP, "source", "description");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .hasMessage(String.format("The currency [%s] is not accepted.", payment.getCurrency()))
                .isInstanceOf(IllegalStateException.class);

        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldThrowWhenCustomerIsNotFound() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... customer not found in DB
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, mock(PaymentRequest.class)))
                .hasMessage(String.format("Customer does not exist for id [%s]", customerId))
                .isInstanceOf(IllegalStateException.class);

        // ... no interactions with cardPaymentCharger
        then(cardPaymentCharger).shouldHaveNoInteractions();
        // ... no interactions with paymentRepository
        then(paymentRepository).shouldHaveNoInteractions();
    }
}