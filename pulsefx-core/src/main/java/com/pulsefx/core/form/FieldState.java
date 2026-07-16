package dev.yasmramos.pulsefx.core.form;

import dev.yasmramos.pulsefx.core.validation.ValidationResult;
import dev.yasmramos.pulsefx.core.validation.Validator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the state of a single form field with validation.
 * 
 * <p>FieldState tracks the value, validation status, and dirty state of a field.
 * It automatically validates when the value changes and provides observable properties.</p>
 * 
 * @param <T> the type of the field value
 * @since 0.1.0
 */
final class FieldState<T> {
    
    private final String fieldName;
    private final javafx.beans.value.ObservableValue<T> valueProperty;
    private final Validator<T> validator;
    private final ReadOnlyBooleanWrapper validWrapper;
    private final ReadOnlyBooleanWrapper dirtyWrapper;
    private final ObservableList<String> errors;
    private T initialValue;
    private boolean hasInitialValue;
    
    /**
     * Constructs a new FieldState.
     * 
     * @param fieldName the name of the field
     * @param valueProperty the observable value holding the field value
     * @param validator the validator for this field
     */
    FieldState(String fieldName, javafx.beans.value.ObservableValue<T> valueProperty, Validator<T> validator) {
        this.fieldName = fieldName;
        this.valueProperty = valueProperty;
        this.validator = validator;
        this.validWrapper = new ReadOnlyBooleanWrapper(true);
        this.dirtyWrapper = new ReadOnlyBooleanWrapper(false);
        this.errors = FXCollections.observableArrayList();
        this.hasInitialValue = false;
        
        // Capture initial value if available
        if (valueProperty.getValue() != null) {
            this.initialValue = valueProperty.getValue();
            this.hasInitialValue = true;
        }
        
        // Listen to value changes
        valueProperty.addListener((obs, oldVal, newVal) -> {
            if (!hasInitialValue) {
                this.initialValue = oldVal;
                this.hasInitialValue = true;
            }
            updateDirtyState();
            validate();
        });
        
        // Initial validation
        validate();
    }
    
    /**
     * Returns the field name.
     * 
     * @return the field name
     */
    String getFieldName() {
        return fieldName;
    }
    
    /**
     * Returns true if the field is valid.
     * 
     * @return true if valid
     */
    boolean isValid() {
        return validWrapper.get();
    }
    
    /**
     * Returns the valid property for binding.
     * 
     * @return read-only boolean property indicating validity
     */
    ReadOnlyBooleanProperty validProperty() {
        return validWrapper.getReadOnlyProperty();
    }
    
    /**
     * Returns true if the field has been modified from its initial value.
     * 
     * @return true if dirty
     */
    boolean isDirty() {
        return dirtyWrapper.get();
    }
    
    /**
     * Returns the dirty property for binding.
     * 
     * @return read-only boolean property indicating dirty state
     */
    ReadOnlyBooleanProperty dirtyProperty() {
        return dirtyWrapper.getReadOnlyProperty();
    }
    
    /**
     * Returns the list of validation errors.
     * 
     * @return unmodifiable list of error messages
     */
    List<String> getErrors() {
        return Collections.unmodifiableList(new ArrayList<>(errors));
    }
    
    /**
     * Returns the errors as an observable list.
     * 
     * @return observable list of error messages
     */
    ObservableList<String> getObservableErrors() {
        return FXCollections.unmodifiableObservableList(errors);
    }
    
    /**
     * Validates the current value synchronously.
     */
    void validate() {
        T value = valueProperty.getValue();
        ValidationResult result = validator.validate(value);
        updateValidationResult(result);
    }
    
    /**
     * Validates the current value asynchronously.
     * 
     * @return a CompletableFuture that completes when validation is done
     */
    CompletableFuture<ValidationResult> validateAsync() {
        return validator.validateAsync(valueProperty.getValue())
                .thenApply(result -> {
                    Platform.runLater(() -> updateValidationResult(result));
                    return result;
                });
    }
    
    /**
     * Resets the field to its initial value and clears dirty state.
     */
    void reset() {
        if (valueProperty instanceof Property<?>) {
            @SuppressWarnings("unchecked")
            Property<T> prop = (Property<T>) valueProperty;
            if (hasInitialValue) {
                prop.setValue(initialValue);
            } else {
                prop.setValue(null);
            }
        }
        markAsClean();
    }
    
    /**
     * Marks the field as clean (not dirty) without changing its value.
     */
    void markAsClean() {
        T currentValue = valueProperty.getValue();
        this.initialValue = currentValue;
        this.hasInitialValue = true;
        dirtyWrapper.set(false);
    }
    
    private void updateDirtyState() {
        T currentValue = valueProperty.getValue();
        if (!hasInitialValue) {
            dirtyWrapper.set(currentValue != null);
        } else {
            dirtyWrapper.set(!Objects.equals(initialValue, currentValue));
        }
    }
    
    private void updateValidationResult(ValidationResult result) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateValidationResult(result));
            return;
        }
        
        errors.clear();
        if (result instanceof ValidationResult.Invalid invalid) {
            errors.addAll(invalid.errors());
            validWrapper.set(false);
        } else {
            validWrapper.set(true);
        }
    }
}
