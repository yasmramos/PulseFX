package com.pulsefx.core.rules;

import com.pulsefx.core.validation.ValidationResult;
import com.pulsefx.core.validation.Validator;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Factory class for common validation rules.
 * 
 * <p>This class provides static factory methods for creating commonly used validators.
 * All rules are null-safe and handle null values appropriately.</p>
 * 
 * @since 0.1.0
 */
public final class Rules {
    
    private Rules() {
        // Prevent instantiation
    }
    
    /**
     * Creates a validator that checks if a value is not null.
     * 
     * @param <T> the type of value to validate
     * @return a validator that passes if the value is not null
     */
    public static <T> Validator<T> nonNull() {
        return value -> {
            if (value == null) {
                return new ValidationResult.Invalid(java.util.List.of("Value must not be null"));
            }
            return ValidationResult.Valid.INSTANCE;
        };
    }
    
    /**
     * Creates a validator that checks if a value is null.
     * 
     * @param <T> the type of value to validate
     * @return a validator that passes if the value is null
     */
    public static <T> Validator<T> isNull() {
        return value -> {
            if (value != null) {
                return new ValidationResult.Invalid(java.util.List.of("Value must be null"));
            }
            return ValidationResult.Valid.INSTANCE;
        };
    }
    
    /**
     * Creates a validator that checks if a String is not empty.
     * 
     * <p>This validator also considers strings with only whitespace as empty.</p>
     * 
     * @return a validator that passes if the string is not empty
     */
    public static Validator<String> nonEmpty() {
        return value -> {
            if (value == null || value.trim().isEmpty()) {
                return new ValidationResult.Invalid(java.util.List.of("Value must not be empty"));
            }
            return ValidationResult.Valid.INSTANCE;
        };
    }
    
    /**
     * Creates a validator that checks if a String has a minimum length.
     * 
     * @param minLength the minimum length (inclusive)
     * @return a validator that passes if the string length is at least minLength
     * @throws IllegalArgumentException if minLength is negative
     */
    public static Validator<String> minLength(int minLength) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be non-negative");
        }
        return value -> {
            if (value == null) {
                return ValidationResult.Valid.INSTANCE;
            }
            if (value.length() < minLength) {
                return new ValidationResult.Invalid(
                    java.util.List.of("Value must be at least " + minLength + " characters long"));
            }
            return ValidationResult.Valid.INSTANCE;
        };
    }
    
    /**
     * Creates a validator that checks if a String has a maximum length.
     * 
     * @param maxLength the maximum length (inclusive)
     * @return a validator that passes if the string length is at most maxLength
     * @throws IllegalArgumentException if maxLength is negative
     */
    public static Validator<String> maxLength(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength must be non-negative");
        }
        return value -> {
            if (value == null) {
                return ValidationResult.Valid.INSTANCE;
            }
            if (value.length() > maxLength) {
                return new ValidationResult.Invalid(
                    java.util.List.of("Value must be at most " + maxLength + " characters long"));
            }
            return ValidationResult.Valid.INSTANCE;
        };
    }
    
    /**
     * Creates a validator that checks if a String matches an email pattern.
     * 
     * <p>This uses a simple regex pattern for email validation. For production use,
     * consider a more comprehensive email validation library.</p>
     * 
     * @return a validator that passes if the string is a valid email format
     */
    public static Validator<String> email() {
        // Simple email regex - good enough for basic validation
        var emailPattern = java.util.regex.Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        );
        return value -> {
            if (value == null || value.trim().isEmpty()) {
                return ValidationResult.Valid.INSTANCE;
            }
            if (!emailPattern.matcher(value).matches()) {
                return new ValidationResult.Invalid(java.util.List.of("Value must be a valid email address"));
            }
            return ValidationResult.Valid.INSTANCE;
        };
    }
    
    /**
     * Creates a validator from a custom predicate.
     * 
     * @param <T> the type of value to validate
     * @param predicate the predicate to test
     * @param errorMessage the error message if the predicate returns false
     * @return a validator that passes if the predicate returns true
     * @throws NullPointerException if predicate or errorMessage is null
     */
    public static <T> Validator<T> satisfies(Predicate<? super T> predicate, String errorMessage) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        return value -> {
            if (predicate.test(value)) {
                return ValidationResult.Valid.INSTANCE;
            }
            return new ValidationResult.Invalid(java.util.List.of(errorMessage));
        };
    }
    
    /**
     * Creates a validator that always passes.
     * 
     * @param <T> the type of value to validate
     * @return a validator that always returns Valid
     */
    public static <T> Validator<T> alwaysValid() {
        return value -> ValidationResult.Valid.INSTANCE;
    }
    
    /**
     * Creates a validator that always fails.
     * 
     * @param <T> the type of value to validate
     * @param reason the reason for failure
     * @return a validator that always returns Invalid
     */
    public static <T> Validator<T> alwaysInvalid(String reason) {
        return value -> new ValidationResult.Invalid(java.util.List.of(reason));
    }
}
