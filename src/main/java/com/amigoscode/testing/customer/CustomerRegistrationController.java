package com.amigoscode.testing.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/customer-registration")
public class CustomerRegistrationController {

    private final CustomerRegistrationService registrationService;

    @Autowired
    public CustomerRegistrationController(CustomerRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PutMapping
    public void registerNewCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        registrationService.registerNewCustomer(request);
    }

}
