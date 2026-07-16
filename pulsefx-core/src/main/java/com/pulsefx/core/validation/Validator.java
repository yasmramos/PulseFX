package com.pulsefx.core.validation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A validator that checks values of type T and returns a validation result.
 * 
 * <p>Validators can be synchronous or asynchronous. Use {@link #validate(Object)}
 * for synchronous validation and {@link #validateAsync(Object)} for asynchronous.</p>
 * 
 * <p>Validators can be composed using {@link #and(Validator)} and {@link #or(Validator)}
 * to create complex validation rules.</p>
 * 
 * @param <T> the type of value to validate
 * @since 0.1.0
 */
@FunctionalInterface
public interface Validator<T> {
    
    /**
     * Validates the given value synchronously.
     * 
     * <p>This method must return quickly. For long-running validations,
     * override {@link #validateAsync(Object)} instead.</p>
     * 
     * @param value the value to validate, may be null
     * @return the validation result
     */
    ValidationResult validate(T value);
    
    /**
     * Validates the given value asynchronously.
     * 
     * <p>The default implementation wraps the synchronous {@link #validate(Object)}
     * in a completed CompletableFuture. Override this method for true async validation.</p>
     * 
     * @param value the value to validate, may be null
     * @return a CompletableFuture that will complete with the validation result
     */
    default CompletableFuture<ValidationResult> validateAsync(T value) {
        return CompletableFuture.completedFuture(validate(value));
    }
    
    /**
     * Combines this validator with another using AND logic.
     * 
     * <p>Both validators must pass for the combined validator to pass.
     * If both fail, all error reasons from both are included.</p>
     * 
     * @param other the other validator to combine with
     * @return a new validator that requires both this and the other to pass
     */
    default Validator<T> and(Validator<T> other) {
        return new AndValidator<>(this, other);
    }
    
    /**
     * Combines this validator with another using OR logic.
     * 
     * <p>At least one validator must pass for the combined validator to pass.
     * Only if both fail will the result be invalid.</p>
     * 
     * @param other the other validator to combine with
     * @return a new validator that requires at least one to pass
     */
    default Validator<T> or(Validator<T> other) {
        return new OrValidator<>(this, other);
    }
    
    /**
     * Creates a validator that negates this validator's result.
     * 
     * @return a new validator that passes when this one fails
     */
    default Validator<T> negate() {
        return value -> {
            ValidationResult result = validate(value);
            return switch (result) {
                case ValidationResult.Valid ignored -> new ValidationResult.Invalid(List.of("Value should not satisfy the condition"));
                case ValidationResult.Invalid ignored -> ValidationResult.Valid.INSTANCE;
            };
        };
    }
}
