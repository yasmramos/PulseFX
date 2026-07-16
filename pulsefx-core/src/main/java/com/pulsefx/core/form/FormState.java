package dev.yasmramos.pulsefx.core.form;

import dev.yasmramos.pulsefx.core.validation.ValidationResult;
import dev.yasmramos.pulsefx.core.validation.Validator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the state of a form with multiple validated fields.
 * 
 * <p>FormState tracks validation status, dirty state (whether fields have been modified),
 * and provides observable properties for UI binding.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * FormState form = FormState.builder()
 *     .field(emailProp, Rules.email().and(Rules.nonEmpty()))
 *     .field(passwordProp, Rules.minLength(8))
 *     .build();
 * 
 * submitButton.disableProperty().bind(form.validProperty().not());
 * }</pre>
 * 
 * @since 0.1.0
 */
public final class FormState {
    
    private final Map<String, FieldState<?>> fields;
    private final ReadOnlyBooleanWrapper validWrapper;
    private final ReadOnlyBooleanWrapper dirtyWrapper;
    private final ReadOnlyStringWrapper errorsWrapper;
    
    private FormState(Builder builder) {
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(builder.fields));
        this.validWrapper = new ReadOnlyBooleanWrapper(true);
        this.dirtyWrapper = new ReadOnlyBooleanWrapper(false);
        this.errorsWrapper = new ReadOnlyStringWrapper("");
        
        initializeListeners();
        updateComputedProperties();
    }
    
    /**
     * Creates a new FormState builder.
     * 
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    private void initializeListeners() {
        for (FieldState<?> field : fields.values()) {
            field.validProperty().addListener((obs, oldVal, newVal) -> updateComputedProperties());
            field.dirtyProperty().addListener((obs, oldVal, newVal) -> updateComputedProperties());
        }
    }
    
    private void updateComputedProperties() {
        boolean allValid = fields.values().stream().allMatch(FieldState::isValid);
        boolean anyDirty = fields.values().stream().anyMatch(FieldState::isDirty);
        
        validWrapper.set(allValid);
        dirtyWrapper.set(anyDirty);
        
        // Collect all errors
        List<String> allErrors = new ArrayList<>();
        for (FieldState<?> field : fields.values()) {
            if (!field.isValid()) {
                field.getErrors().forEach(error -> allErrors.add(field.getFieldName() + ": " + error));
            }
        }
        errorsWrapper.set(String.join(", ", allErrors));
    }
    
    /**
     * Returns true if all fields are valid.
     * 
     * @return true if the form is valid
     */
    public boolean isValid() {
        return validWrapper.get();
    }
    
    /**
     * Returns the valid property for binding.
     * 
     * @return read-only boolean property indicating form validity
     */
    public ReadOnlyBooleanProperty validProperty() {
        return validWrapper.getReadOnlyProperty();
    }
    
    /**
     * Returns true if any field has been modified.
     * 
     * @return true if the form is dirty
     */
    public boolean isDirty() {
        return dirtyWrapper.get();
    }
    
    /**
     * Returns the dirty property for binding.
     * 
     * @return read-only boolean property indicating dirty state
     */
    public ReadOnlyBooleanProperty dirtyProperty() {
        return dirtyWrapper.getReadOnlyProperty();
    }
    
    /**
     * Returns a comma-separated string of all validation errors.
     * 
     * @return string containing all error messages
     */
    public String getErrors() {
        return errorsWrapper.get();
    }
    
    /**
     * Returns the errors property for binding.
     * 
     * @return read-only string property containing all errors
     */
    public ReadOnlyStringProperty errorsProperty() {
        return errorsWrapper.getReadOnlyProperty();
    }
    
    /**
     * Checks if a specific field has validation errors.
     * 
     * @param fieldName the name of the field to check
     * @return true if the field has errors
     */
    public boolean hasFieldErrors(String fieldName) {
        FieldState<?> field = fields.get(fieldName);
        return field != null && !field.isValid();
    }
    
    /**
     * Gets the validation errors for a specific field.
     * 
     * @param fieldName the name of the field
     * @return list of error messages for the field, or empty list if field doesn't exist or is valid
     */
    public List<String> getFieldErrors(String fieldName) {
        FieldState<?> field = fields.get(fieldName);
        if (field == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(field.getErrors());
    }
    
    /**
     * Resets all fields to their initial values and clears dirty state.
     */
    public void reset() {
        fields.values().forEach(FieldState::reset);
    }
    
    /**
     * Marks all fields as clean (not dirty) without changing their values.
     * Use this after successfully saving the form.
     */
    public void markAsClean() {
        fields.values().forEach(FieldState::markAsClean);
    }
    
    /**
     * Validates all fields immediately.
     * 
     * @return true if all fields are valid after validation
     */
    public boolean validateAll() {
        fields.values().forEach(FieldState::validate);
        return isValid();
    }
    
    /**
     * Gets the names of all fields in this form.
     * 
     * @return set of field names
     */
    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(fields.keySet());
    }
    
    /**
     * Builder for creating FormState instances.
     */
    public static final class Builder {
        private final Map<String, FieldState<?>> fields = new LinkedHashMap<>();
        
        private Builder() {
        }
        
        /**
         * Adds a field to the form with a validator.
         * 
         * @param <T> the type of the field value
         * @param fieldName the name of the field (used for error reporting)
         * @param valueProperty the JavaFX property holding the field value
         * @param validator the validator for this field
         * @return this builder for chaining
         */
        public <T> Builder field(String fieldName, javafx.beans.value.ObservableValue<T> valueProperty, Validator<T> validator) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            Objects.requireNonNull(valueProperty, "valueProperty must not be null");
            Objects.requireNonNull(validator, "validator must not be null");
            
            fields.put(fieldName, new FieldState<>(fieldName, valueProperty, validator));
            return this;
        }
        
        /**
         * Adds a field to the form with a validator, using property's bean name as field name.
         * 
         * @param <T> the type of the field value
         * @param valueProperty the JavaFX property holding the field value
         * @param validator the validator for this field
         * @return this builder for chaining
         */
        public <T> Builder field(javafx.beans.value.ObservableValue<T> valueProperty, Validator<T> validator) {
            Objects.requireNonNull(valueProperty, "valueProperty must not be null");
            Objects.requireNonNull(validator, "validator must not be null");
            
            String fieldName = "field_" + fields.size();
            
            return field(fieldName, valueProperty, validator);
        }
        
        /**
         * Builds the FormState instance.
         * 
         * @return a new FormState with all configured fields
         */
        public FormState build() {
            return new FormState(this);
        }
    }
}
