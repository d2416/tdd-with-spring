package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

class CustomerRegistrationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    private CustomerRegistrationService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new CustomerRegistrationService(customerRepository, phoneNumberValidator);
    }

    @Test
    void itShouldSaveNewCustomer() {
        // Given
        String phoneNumber = "000011";
        Customer customer = new Customer(UUID.randomUUID(), "Juan", phoneNumber);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... validate phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // ... find customer in DB
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).isEqualTo(customer);
    }

    @Test
    void itShouldNotSaveNewCustomerWhenPhoneNumberIsInvalid() {
        // Given
        String phoneNumber = "000011";
        Customer customer = new Customer(UUID.randomUUID(), "Juan", phoneNumber);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... validate phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(false);

        // When
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Phone number [%s] is not valid.", phoneNumber));

        // Then
        then(customerRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotSaveCustomerWhenCustomerExists() {
        // Given
        String phoneNumber = "000012";
        Customer customer = new Customer(UUID.randomUUID(), "Juan", phoneNumber);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... validate phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // ... find customer in DB
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.of(customer));

        // When
        underTest.registerNewCustomer(request);

        // Then
        // Option 1: when we know the exactly methods that will be called
        then(customerRepository).should(never()).save(any());

        // Option 2: if we don't know the exactly methods that will not be called
        // then(customerRepository).should().selectCustomerByPhoneNumber(phoneNumber);
        // then(customerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldThrowExceptionWhenPhoneNumberIsTaken() {
        // Given
        String phoneNumber = "000033";
        Customer customer = new Customer(UUID.randomUUID(), "Juan", phoneNumber);
        Customer customer2 = new Customer(UUID.randomUUID(), "Alberto", phoneNumber);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... validate phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // ... find customer in DB
        given(customerRepository.selectCustomerByPhoneNumber(any())).willReturn(Optional.of(customer2));

        // When
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .hasMessage("The phone number [000033] belongs to another customer.")
                .isInstanceOf(IllegalArgumentException.class);
        then(customerRepository).should(never()).save(any());
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        // Given
        String phoneNumber = "000011";
        Customer customer = new Customer(null, "Juan", phoneNumber);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... validate phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // ... find customer in DB
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).isEqualToIgnoringGivenFields(customer, "id");
        assertThat(customerArgumentCaptorValue.getId()).isNotNull();
    }

}