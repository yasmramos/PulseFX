package com.pulsefx.core.rules;

import com.pulsefx.core.validation.ValidationResult;
import com.pulsefx.core.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Rules")
class RulesTest {

    @Nested
    @DisplayName("nonNull rule")
    class NonNullTests {

        private final Validator<Object> validator = Rules.nonNull();

        @Test
        @DisplayName("should pass when value is not null")
        void passesWhenNotNull() {
            ValidationResult result = validator.validate("test");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when value is null")
        void failsWhenNull() {
            ValidationResult result = validator.validate(null);
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).contains("Value must not be null");
        }
    }

    @Nested
    @DisplayName("isNull rule")
    class IsNullTests {

        private final Validator<Object> validator = Rules.isNull();

        @Test
        @DisplayName("should pass when value is null")
        void passesWhenNull() {
            ValidationResult result = validator.validate(null);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when value is not null")
        void failsWhenNotNull() {
            ValidationResult result = validator.validate("test");
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).contains("Value must be null");
        }
    }

    @Nested
    @DisplayName("nonEmpty rule")
    class NonEmptyTests {

        private final Validator<String> validator = Rules.nonEmpty();

        @Test
        @DisplayName("should pass when string has content")
        void passesWhenHasContent() {
            ValidationResult result = validator.validate("Hello");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when string is null")
        void failsWhenNull() {
            ValidationResult result = validator.validate(null);
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should fail when string is empty")
        void failsWhenEmpty() {
            ValidationResult result = validator.validate("");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should fail when string is whitespace only")
        void failsWhenWhitespaceOnly() {
            ValidationResult result = validator.validate("   ");
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("minLength rule")
    class MinLengthTests {

        @Test
        @DisplayName("should throw when minLength is negative")
        void throwsWhenNegativeMinLength() {
            assertThatThrownBy(() -> Rules.minLength(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("minLength must be non-negative");
        }

        @Test
        @DisplayName("should pass when string meets minimum length")
        void passesWhenMeetsMinLength() {
            Validator<String> validator = Rules.minLength(5);
            ValidationResult result = validator.validate("Hello");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should pass when string exceeds minimum length")
        void passesWhenExceedsMinLength() {
            Validator<String> validator = Rules.minLength(3);
            ValidationResult result = validator.validate("Hello");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when string is shorter than minimum")
        void failsWhenShorterThanMin() {
            Validator<String> validator = Rules.minLength(10);
            ValidationResult result = validator.validate("Hi");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should pass when value is null")
        void passesWhenNull() {
            Validator<String> validator = Rules.minLength(5);
            ValidationResult result = validator.validate(null);
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("maxLength rule")
    class MaxLengthTests {

        @Test
        @DisplayName("should throw when maxLength is negative")
        void throwsWhenNegativeMaxLength() {
            assertThatThrownBy(() -> Rules.maxLength(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxLength must be non-negative");
        }

        @Test
        @DisplayName("should pass when string is under maximum length")
        void passesWhenUnderMaxLength() {
            Validator<String> validator = Rules.maxLength(10);
            ValidationResult result = validator.validate("Hi");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should pass when string equals maximum length")
        void passesWhenEqualsMaxLength() {
            Validator<String> validator = Rules.maxLength(5);
            ValidationResult result = validator.validate("Hello");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when string exceeds maximum length")
        void failsWhenExceedsMaxLength() {
            Validator<String> validator = Rules.maxLength(2);
            ValidationResult result = validator.validate("Hello");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should pass when value is null")
        void passesWhenNull() {
            Validator<String> validator = Rules.maxLength(5);
            ValidationResult result = validator.validate(null);
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("email rule")
    class EmailTests {

        private final Validator<String> validator = Rules.email();

        @Test
        @DisplayName("should pass for valid email")
        void passesForValidEmail() {
            ValidationResult result = validator.validate("test@example.com");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should pass for valid email with subdomain")
        void passesForValidEmailWithSubdomain() {
            ValidationResult result = validator.validate("user@mail.example.com");
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail for email without @")
        void failsForEmailWithoutAt() {
            ValidationResult result = validator.validate("invalid");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should fail for email without domain")
        void failsForEmailWithoutDomain() {
            ValidationResult result = validator.validate("test@");
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should pass when value is null")
        void passesWhenNull() {
            ValidationResult result = validator.validate(null);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should pass when value is empty")
        void passesWhenEmpty() {
            ValidationResult result = validator.validate("");
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("satisfies rule")
    class SatisfiesTests {

        @Test
        @DisplayName("should pass when predicate returns true")
        void passesWhenPredicateTrue() {
            Validator<Integer> validator = Rules.satisfies(x -> x > 10, "Must be greater than 10");
            ValidationResult result = validator.validate(15);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should fail when predicate returns false")
        void failsWhenPredicateFalse() {
            Validator<Integer> validator = Rules.satisfies(x -> x > 10, "Must be greater than 10");
            ValidationResult result = validator.validate(5);
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).contains("Must be greater than 10");
        }

        @Test
        @DisplayName("should throw when predicate is null")
        void throwsWhenPredicateNull() {
            assertThatThrownBy(() -> Rules.satisfies(null, "error"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("predicate must not be null");
        }

        @Test
        @DisplayName("should throw when error message is null")
        void throwsWhenErrorMessageNull() {
            assertThatThrownBy(() -> Rules.satisfies(x -> true, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("errorMessage must not be null");
        }
    }

    @Nested
    @DisplayName("alwaysValid rule")
    class AlwaysValidTests {

        private final Validator<String> validator = Rules.alwaysValid();

        @Test
        @DisplayName("should always pass")
        void alwaysPasses() {
            assertThat(validator.validate("anything").isValid()).isTrue();
            assertThat(validator.validate(null).isValid()).isTrue();
            assertThat(validator.validate("").isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("alwaysInvalid rule")
    class AlwaysInvalidTests {

        private final Validator<String> validator = Rules.alwaysInvalid("Always fails");

        @Test
        @DisplayName("should always fail with provided reason")
        void alwaysFails() {
            ValidationResult result = validator.validate("anything");
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).containsExactly("Always fails");
        }
    }
}
