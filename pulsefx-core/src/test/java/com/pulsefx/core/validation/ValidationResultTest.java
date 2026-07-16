package com.pulsefx.core.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ValidationResult")
class ValidationResultTest {

    @Nested
    @DisplayName("Valid result")
    class ValidResultTests {

        @Test
        @DisplayName("should return true for isValid()")
        void isValidReturnsTrue() {
            ValidationResult result = ValidationResult.Valid.INSTANCE;
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should be a singleton")
        void isSingleton() {
            ValidationResult result1 = ValidationResult.Valid.INSTANCE;
            ValidationResult result2 = ValidationResult.Valid.INSTANCE;
            assertThat(result1).isSameAs(result2);
        }
    }

    @Nested
    @DisplayName("Invalid result")
    class InvalidResultTests {

        @Test
        @DisplayName("should return false for isValid()")
        void isValidReturnsFalse() {
            ValidationResult result = new ValidationResult.Invalid(List.of("Error message"));
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should return errors list")
        void errorsReturnsList() {
            List<String> errors = List.of("Error 1", "Error 2");
            ValidationResult result = new ValidationResult.Invalid(errors);
            assertThat(result.errors()).containsExactlyElementsOf(errors);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when errors is null")
        void constructorThrowsWhenErrorsIsNull() {
            assertThatThrownBy(() -> new ValidationResult.Invalid(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid result must have at least one reason");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when errors is empty")
        void constructorThrowsWhenErrorsIsEmpty() {
            assertThatThrownBy(() -> new ValidationResult.Invalid(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid result must have at least one reason");
        }

        @Test
        @DisplayName("should accept single error")
        void acceptsSingleError() {
            ValidationResult result = new ValidationResult.Invalid(List.of("Single error"));
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().get(0)).isEqualTo("Single error");
        }

        @Test
        @DisplayName("should accept multiple errors")
        void acceptsMultipleErrors() {
            List<String> errors = List.of("Error 1", "Error 2", "Error 3");
            ValidationResult result = new ValidationResult.Invalid(errors);
            assertThat(result.errors()).containsExactlyElementsOf(errors);
        }
    }

    @Nested
    @DisplayName("Pattern matching")
    class PatternMatchingTests {

        @Test
        @DisplayName("should match Valid case in switch expression")
        void matchesValidInSwitch() {
            ValidationResult result = ValidationResult.Valid.INSTANCE;
            String output = switch (result) {
                case ValidationResult.Valid ignored -> "valid";
                case ValidationResult.Invalid ignored -> "invalid";
            };
            assertThat(output).isEqualTo("valid");
        }

        @Test
        @DisplayName("should match Invalid case in switch expression")
        void matchesInvalidInSwitch() {
            ValidationResult result = new ValidationResult.Invalid(List.of("Error"));
            String output = switch (result) {
                case ValidationResult.Valid ignored -> "valid";
                case ValidationResult.Invalid ignored -> "invalid";
            };
            assertThat(output).isEqualTo("invalid");
        }

        @Test
        @DisplayName("should extract errors using pattern matching")
        void extractErrorsWithPatternMatching() {
            ValidationResult result = new ValidationResult.Invalid(List.of("Error 1", "Error 2"));
            List<String> extractedErrors = switch (result) {
                case ValidationResult.Valid ignored -> List.of();
                case ValidationResult.Invalid invalid -> invalid.errors();
            };
            assertThat(extractedErrors).containsExactly("Error 1", "Error 2");
        }
    }
}
