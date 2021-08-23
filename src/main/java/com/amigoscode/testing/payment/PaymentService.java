package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.EUR, Currency.GBP, Currency.USD);

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final CardPaymentCharger paymentCharger;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, CustomerRepository customerRepository, CardPaymentCharger paymentCharger) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.paymentCharger = paymentCharger;
    }

    public void chargeCard(UUID customerId, PaymentRequest paymentRequest) {

        boolean isCustomerPresent = customerRepository.findById(customerId).isPresent();

        if (!isCustomerPresent) {
            throw new IllegalStateException(String.format("Customer does not exist for id [%s]", customerId));
        }

        Payment payment = paymentRequest.getPayment();

        boolean isCurrencyAccepted = ACCEPTED_CURRENCIES.stream().anyMatch(currency -> currency.equals(payment.getCurrency()));

        if (!isCurrencyAccepted) {
            throw new IllegalStateException(String.format("The currency [%s] is not accepted.", payment.getCurrency()));
        }

        CardPaymentCharge charge = paymentCharger.chargeCard(payment.getSource(), payment.getAmount(), payment.getCurrency(), payment.getDescription());

        if (!charge.isCardDebited()) {
            throw new IllegalStateException("The card was not debited.");
        }

        payment.setCustomerId(customerId);

        paymentRepository.save(payment);

        // TODO: send sms

    }
}
