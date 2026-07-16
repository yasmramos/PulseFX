package com.pulsefx.core.form;

import com.pulsefx.core.validation.ValidationResult;
import com.pulsefx.core.validation.Validator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FormState")
class FormStateTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should create form with single field")
        void createsFormWithSingleField() {
            StringProperty prop = new SimpleStringProperty("");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", prop, validator)
                    .build();

            assertThat(form.getFieldNames()).containsExactly("email");
        }

        @Test
        @DisplayName("should create form with multiple fields")
        void createsFormWithMultipleFields() {
            StringProperty emailProp = new SimpleStringProperty("");
            StringProperty passwordProp = new SimpleStringProperty("");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .field("password", passwordProp, validator)
                    .build();

            assertThat(form.getFieldNames()).containsExactly("email", "password");
        }

        @Test
        @DisplayName("should throw when fieldName is null")
        void throwsWhenFieldNameNull() {
            StringProperty prop = new SimpleStringProperty("");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            assertThatThrownBy(() -> FormState.builder()
                    .field(null, prop, validator))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("fieldName must not be null");
        }

        @Test
        @DisplayName("should throw when valueProperty is null")
        void throwsWhenValuePropertyNull() {
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            assertThatThrownBy(() -> FormState.builder()
                    .field("email", null, validator))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("valueProperty must not be null");
        }

        @Test
        @DisplayName("should throw when validator is null")
        void throwsWhenValidatorNull() {
            StringProperty prop = new SimpleStringProperty("");

            assertThatThrownBy(() -> FormState.builder()
                    .field("email", prop, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("validator must not be null");
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("should be valid when all fields pass validation")
        void isValidWhenAllFieldsPass() {
            StringProperty emailProp = new SimpleStringProperty("test@example.com");
            Validator<String> emailValidator = value -> 
                (value != null && value.contains("@")) 
                    ? ValidationResult.Valid.INSTANCE 
                    : new ValidationResult.Invalid(List.of("Invalid email"));

            FormState form = FormState.builder()
                    .field("email", emailProp, emailValidator)
                    .build();

            assertThat(form.isValid()).isTrue();
        }

        @Test
        @DisplayName("should be invalid when any field fails validation")
        void isInvalidWhenAnyFieldFails() {
            StringProperty emailProp = new SimpleStringProperty("invalid");
            Validator<String> emailValidator = value -> 
                (value != null && value.contains("@")) 
                    ? ValidationResult.Valid.INSTANCE 
                    : new ValidationResult.Invalid(List.of("Invalid email"));

            FormState form = FormState.builder()
                    .field("email", emailProp, emailValidator)
                    .build();

            assertThat(form.isValid()).isFalse();
        }

        @Test
        @DisplayName("should update validity when field value changes")
        void updatesValidityOnValueChange() {
            StringProperty emailProp = new SimpleStringProperty("invalid");
            Validator<String> emailValidator = value -> 
                (value != null && value.contains("@")) 
                    ? ValidationResult.Valid.INSTANCE 
                    : new ValidationResult.Invalid(List.of("Invalid email"));

            FormState form = FormState.builder()
                    .field("email", emailProp, emailValidator)
                    .build();

            assertThat(form.isValid()).isFalse();

            emailProp.set("valid@example.com");

            assertThat(form.isValid()).isTrue();
        }

        @Test
        @DisplayName("should track errors for invalid fields")
        void tracksErrorsForInvalidFields() {
            StringProperty emailProp = new SimpleStringProperty("invalid");
            Validator<String> emailValidator = value -> 
                new ValidationResult.Invalid(List.of("Must contain @"));

            FormState form = FormState.builder()
                    .field("email", emailProp, emailValidator)
                    .build();

            assertThat(form.getErrors()).contains("email: Must contain @");
        }

        @Test
        @DisplayName("should check field-specific errors")
        void checksFieldSpecificErrors() {
            StringProperty emailProp = new SimpleStringProperty("invalid");
            Validator<String> emailValidator = value -> 
                new ValidationResult.Invalid(List.of("Must contain @"));

            FormState form = FormState.builder()
                    .field("email", emailProp, emailValidator)
                    .build();

            assertThat(form.hasFieldErrors("email")).isTrue();
        }

        @Test
        @DisplayName("should return empty list for non-existent field errors")
        void returnsEmptyListForNonExistentField() {
            StringProperty emailProp = new SimpleStringProperty("test@example.com");
            Validator<String> emailValidator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", emailProp, emailValidator)
                    .build();

            assertThat(form.getFieldErrors("nonexistent")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Dirty tracking")
    class DirtyTrackingTests {

        @Test
        @DisplayName("should not be dirty initially")
        void notDirtyInitially() {
            StringProperty emailProp = new SimpleStringProperty("initial@example.com");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            assertThat(form.isDirty()).isFalse();
        }

        @Test
        @DisplayName("should be dirty when field value changes")
        void dirtyWhenValueChanges() {
            StringProperty emailProp = new SimpleStringProperty("initial@example.com");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            emailProp.set("changed@example.com");

            assertThat(form.isDirty()).isTrue();
        }

        @Test
        @DisplayName("should not be dirty after markAsClean")
        void notDirtyAfterMarkAsClean() {
            StringProperty emailProp = new SimpleStringProperty("initial@example.com");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            emailProp.set("changed@example.com");
            assertThat(form.isDirty()).isTrue();

            form.markAsClean();

            assertThat(form.isDirty()).isFalse();
        }

        @Test
        @DisplayName("should be clean after reset")
        void cleanAfterReset() {
            StringProperty emailProp = new SimpleStringProperty("initial@example.com");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            emailProp.set("changed@example.com");
            assertThat(form.isDirty()).isTrue();

            form.reset();

            assertThat(form.isDirty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Observable properties")
    class ObservablePropertiesTests {

        @Test
        @DisplayName("should provide valid property for binding")
        void providesValidProperty() {
            StringProperty emailProp = new SimpleStringProperty("test@example.com");
            Validator<String> validator = value -> 
                (value != null && value.contains("@")) 
                    ? ValidationResult.Valid.INSTANCE 
                    : new ValidationResult.Invalid(List.of("Invalid email"));

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            assertThat(form.validProperty().getValue()).isTrue();

            emailProp.set("invalid");

            assertThat(form.validProperty().getValue()).isFalse();
        }

        @Test
        @DisplayName("should provide dirty property for binding")
        void providesDirtyProperty() {
            StringProperty emailProp = new SimpleStringProperty("initial@example.com");
            Validator<String> validator = value -> ValidationResult.Valid.INSTANCE;

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            assertThat(form.dirtyProperty().getValue()).isFalse();

            emailProp.set("changed@example.com");

            assertThat(form.dirtyProperty().getValue()).isTrue();
        }

        @Test
        @DisplayName("should provide errors property for binding")
        void providesErrorsProperty() {
            StringProperty emailProp = new SimpleStringProperty("invalid");
            Validator<String> validator = value -> 
                new ValidationResult.Invalid(List.of("Must contain @"));

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            assertThat(form.errorsProperty().getValue()).contains("email: Must contain @");
        }
    }

    @Nested
    @DisplayName("validateAll")
    class ValidateAllTests {

        @Test
        @DisplayName("should validate all fields and return true when all valid")
        void validatesAllAndReturnsTrue() {
            StringProperty emailProp = new SimpleStringProperty("test@example.com");
            Validator<String> validator = value -> 
                (value != null && value.contains("@")) 
                    ? ValidationResult.Valid.INSTANCE 
                    : new ValidationResult.Invalid(List.of("Invalid email"));

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            boolean result = form.validateAll();

            assertThat(result).isTrue();
            assertThat(form.isValid()).isTrue();
        }

        @Test
        @DisplayName("should validate all fields and return false when any invalid")
        void validatesAllAndReturnsFalse() {
            StringProperty emailProp = new SimpleStringProperty("invalid");
            Validator<String> validator = value -> 
                (value != null && value.contains("@")) 
                    ? ValidationResult.Valid.INSTANCE 
                    : new ValidationResult.Invalid(List.of("Invalid email"));

            FormState form = FormState.builder()
                    .field("email", emailProp, validator)
                    .build();

            boolean result = form.validateAll();

            assertThat(result).isFalse();
            assertThat(form.isValid()).isFalse();
        }
    }
}
