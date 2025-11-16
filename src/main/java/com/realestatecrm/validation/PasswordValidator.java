package com.realestatecrm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Check minimum length
        if (password.length() < minLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password must be at least " + minLength + " characters long"
            ).addConstraintViolation();
            return false;
        }

        StringBuilder errorMessage = new StringBuilder("Password must contain:");
        boolean isValid = true;

        // Check for uppercase letter
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            errorMessage.append(" an uppercase letter,");
            isValid = false;
        }

        // Check for lowercase letter
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            errorMessage.append(" a lowercase letter,");
            isValid = false;
        }

        // Check for digit
        if (requireDigit && !password.matches(".*\\d.*")) {
            errorMessage.append(" a digit,");
            isValid = false;
        }

        // Check for special character
        if (requireSpecialChar && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errorMessage.append(" a special character,");
            isValid = false;
        }

        if (!isValid) {
            // Remove trailing comma
            String message = errorMessage.toString().replaceAll(",$", "");
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }

        return isValid;
    }
}
