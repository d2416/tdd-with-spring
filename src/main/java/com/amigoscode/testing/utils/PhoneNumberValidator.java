package com.amigoscode.testing.utils;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class PhoneNumberValidator implements Predicate<String> {

    @Override
    public boolean test(String phoneNumber) {

        return phoneNumber.length() == 12 && phoneNumber.startsWith("+33");
    }
}
