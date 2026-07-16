package com.pulsefx.core.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Validator")
class ValidatorTest {

    @Nested
    @DisplayName("Basic validation")
    class BasicValidationTests {

        private final Validator<String> nonEmptyValidator = value -> {
            if (value == null || value.trim().isEmpty()) {
                return new ValidationResult.Invalid(List.of("Value must not be empty"));
            }
            return ValidationResult.Valid.INSTANCE;
        };

        @Test
        @DisplayName("should return Valid when validation passes")
        void returnsValidWhenPasses() {
            ValidationResult result = nonEmptyValidator.validate("Hello");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should return Invalid when validation fails")
        void returnsInvalidWhenFails() {
            ValidationResult result = nonEmptyValidator.validate("");
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).contains("Value must not be empty");
        }

        @Test
        @DisplayName("should handle null values")
        void handlesNullValues() {
            ValidationResult result = nonEmptyValidator.validate(null);
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Async validation")
    class AsyncValidationTests {

        private final Validator<String> asyncValidator = value -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (value == null || value.isEmpty()) {
                return new ValidationResult.Invalid(List.of("Async validation failed"));
            }
            return ValidationResult.Valid.INSTANCE;
        };

        @Test
        @DisplayName("should complete future with validation result")
        void completesFutureWithResult() throws ExecutionException, InterruptedException {
            CompletableFuture<ValidationResult> future = asyncValidator.validateAsync("test");
            assertThat(future).isCompleted();
            ValidationResult result = future.get();
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should complete future with invalid result on failure")
        void completesFutureWithInvalidResult() throws ExecutionException, InterruptedException {
            CompletableFuture<ValidationResult> future = asyncValidator.validateAsync("");
            assertThat(future).isCompleted();
            ValidationResult result = future.get();
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("AND composition")
    class AndCompositionTests {

        private final Validator<String> nonEmpty = value -> 
            (value != null && !value.isEmpty()) 
                ? ValidationResult.Valid.INSTANCE 
                : new ValidationResult.Invalid(List.of("Must not be empty"));

        private final Validator<String> minLength5 = value -> 
            (value != null && value.length() >= 5) 
                ? ValidationResult.Valid.INSTANCE 
                : new ValidationResult.Invalid(List.of("Must be at least 5 characters"));

        @Test
        @DisplayName("should pass when both validators pass")
        void passesWhenBothPass() {
            Validator<String> combined = nonEmpty.and(minLength5);
            ValidationResult result = combined.validate("Hello World");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when first validator fails")
        void failsWhenFirstFails() {
            Validator<String> combined = nonEmpty.and(minLength5);
            ValidationResult result = combined.validate("");
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).contains("Must not be empty");
        }

        @Test
        @DisplayName("should fail when second validator fails")
        void failsWhenSecondFails() {
            Validator<String> combined = nonEmpty.and(minLength5);
            ValidationResult result = combined.validate("Hi");
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).contains("Must be at least 5 characters");
        }

        @Test
        @DisplayName("should combine reasons when both fail")
        void combinesReasonsWhenBothFail() {
            Validator<String> combined = nonEmpty.and(minLength5);
            ValidationResult result = combined.validate("");
            assertThat(result.errors()).hasSize(2);
            assertThat(result.errors()).contains("Must not be empty", "Must be at least 5 characters");
        }
    }

    @Nested
    @DisplayName("OR composition")
    class OrCompositionTests {

        private final Validator<String> isEmail = value -> 
            (value != null && value.contains("@")) 
                ? ValidationResult.Valid.INSTANCE 
                : new ValidationResult.Invalid(List.of("Must be an email"));

        private final Validator<String> isPhone = value -> 
            (value != null && value.matches("\\d+")) 
                ? ValidationResult.Valid.INSTANCE 
                : new ValidationResult.Invalid(List.of("Must be a phone number"));

        @Test
        @DisplayName("should pass when first validator passes")
        void passesWhenFirstPasses() {
            Validator<String> combined = isEmail.or(isPhone);
            ValidationResult result = combined.validate("test@example.com");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should pass when second validator passes")
        void passesWhenSecondPasses() {
            Validator<String> combined = isEmail.or(isPhone);
            ValidationResult result = combined.validate("123456789");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when both validators fail")
        void failsWhenBothFail() {
            Validator<String> combined = isEmail.or(isPhone);
            ValidationResult result = combined.validate("invalid");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should combine reasons when both fail")
        void combinesReasonsWhenBothFail() {
            Validator<String> combined = isEmail.or(isPhone);
            ValidationResult result = combined.validate("invalid");
            assertThat(result.errors()).hasSize(2);
            assertThat(result.errors()).contains("Must be an email", "Must be a phone number");
        }
    }

    @Nested
    @DisplayName("Negation")
    class NegationTests {

        private final Validator<Integer> isEven = value -> 
            (value % 2 == 0) 
                ? ValidationResult.Valid.INSTANCE 
                : new ValidationResult.Invalid(List.of("Must be even"));

        @Test
        @DisplayName("should invert valid to invalid")
        void invertsValidToInvalid() {
            Validator<Integer> notEven = isEven.negate();
            ValidationResult result = notEven.validate(4);  // 4 es par, isEven lo pasa, notEven lo falla
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should invert invalid to valid")
        void invertsInvalidToValid() {
            Validator<Integer> notEven = isEven.negate();
            ValidationResult result = notEven.validate(3);  // 3 es impar, isEven lo falla, notEven lo pasa
            assertThat(result.isValid()).isTrue();
        }
    }
}
