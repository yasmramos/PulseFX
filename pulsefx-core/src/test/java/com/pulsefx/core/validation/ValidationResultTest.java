package com.pulsefx.core.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ValidationResult}.
 */
@DisplayName("ValidationResult")
class ValidationResultTest {

    @Nested
    @DisplayName("Valid case")
    class ValidCase {

        @Test
        @DisplayName("should create valid result with value")
        void shouldCreateValidResult() {
            String testValue = "test";
            ValidationResult result = ValidationResult.valid(testValue);

            assertThat(result.isValid()).isTrue();
            assertThat(result.value()).isEqualTo(testValue);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("should create valid result with null value")
        void shouldCreateValidResultWithNull() {
            ValidationResult result = ValidationResult.valid(null);

            assertThat(result.isValid()).isTrue();
            assertThat(result.value()).isNull();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("should match Valid case in pattern matching")
        void shouldMatchValidCase() {
            ValidationResult result = ValidationResult.valid("success");

            String output;
            if (result instanceof ValidationResult.Valid valid) {
                output = "Valid: " + valid.value();
            } else {
                output = "Invalid";
            }

            assertThat(output).isEqualTo("Valid: success");
        }
    }

    @Nested
    @DisplayName("Invalid case")
    class InvalidCase {

        @Test
        @DisplayName("should create invalid result with single error")
        void shouldCreateInvalidResultWithSingleError() {
            ValidationResult result = ValidationResult.invalid("Required field");

            assertThat(result.isValid()).isFalse();
            assertThat(result.value()).isNull();
            assertThat(result.errors()).containsExactly("Required field");
        }

        @Test
        @DisplayName("should create invalid result with multiple errors")
        void shouldCreateInvalidResultWithMultipleErrors() {
            List<String> errors = List.of("Error 1", "Error 2", "Error 3");
            ValidationResult result = ValidationResult.invalid(errors);

            assertThat(result.isValid()).isFalse();
            assertThat(result.value()).isNull();
            assertThat(result.errors()).containsExactlyElementsOf(errors);
        }

        @Test
        @DisplayName("should match Invalid case in pattern matching")
        void shouldMatchInvalidCase() {
            ValidationResult result = ValidationResult.invalid(List.of("Failed"));

            String output;
            if (result instanceof ValidationResult.Invalid invalid) {
                output = "Invalid: " + invalid.errors().size() + " errors";
            } else {
                output = "Valid";
            }

            assertThat(output).isEqualTo("Invalid: 1 errors");
        }

        @Test
        @DisplayName("should extract errors from invalid result")
        void shouldExtractErrors() {
            ValidationResult result = ValidationResult.invalid(List.of("Error A", "Error B"));

            List<String> extractedErrors;
            if (result instanceof ValidationResult.Invalid invalid) {
                extractedErrors = invalid.errors();
            } else {
                extractedErrors = List.of();
            }

            assertThat(extractedErrors).containsExactly("Error A", "Error B");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both are Valid with same value")
        void shouldBeEqualForValidResults() {
            ValidationResult r1 = ValidationResult.valid("same");
            ValidationResult r2 = ValidationResult.valid("same");

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("should be equal when both are Invalid with same errors")
        void shouldBeEqualForInvalidResults() {
            List<String> errors = List.of("error");
            ValidationResult r1 = ValidationResult.invalid(errors);
            ValidationResult r2 = ValidationResult.invalid(errors);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when one is Valid and other is Invalid")
        void shouldNotBeEqualForDifferentTypes() {
            ValidationResult valid = ValidationResult.valid("value");
            ValidationResult invalid = ValidationResult.invalid("error");

            assertThat(valid).isNotEqualTo(invalid);
        }
    }
}
