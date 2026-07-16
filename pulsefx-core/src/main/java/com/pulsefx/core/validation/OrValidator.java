package com.pulsefx.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A validator that combines two validators using OR logic.
 * 
 * <p>At least one validator must pass for the combined validator to pass.
 * Only if both fail will the result be invalid.</p>
 * 
 * @param <T> the type of value to validate
 * @since 0.1.0
 */
final class OrValidator<T> implements Validator<T> {
    
    private final Validator<T> first;
    private final Validator<T> second;
    
    /**
     * Constructs a new OrValidator.
     * 
     * @param first the first validator
     * @param second the second validator
     */
    OrValidator(Validator<T> first, Validator<T> second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public ValidationResult validate(T value) {
        ValidationResult firstResult = first.validate(value);
        
        if (firstResult.isValid()) {
            return ValidationResult.Valid.INSTANCE;
        }
        
        ValidationResult secondResult = second.validate(value);
        if (secondResult.isValid()) {
            return ValidationResult.Valid.INSTANCE;
        }
        
        // Both failed, combine reasons
        List<String> allReasons = new ArrayList<>();
        if (firstResult instanceof ValidationResult.Invalid invalid) {
            invalid.errors().forEach(allReasons::add);
        }
        if (secondResult instanceof ValidationResult.Invalid invalid) {
            invalid.errors().forEach(allReasons::add);
        }
        
        return new ValidationResult.Invalid(allReasons);
    }
    
    @Override
    public CompletableFuture<ValidationResult> validateAsync(T value) {
        return first.validateAsync(value).thenCompose(firstResult -> {
            if (firstResult.isValid()) {
                return CompletableFuture.completedFuture(ValidationResult.Valid.INSTANCE);
            }
            return second.validateAsync(value).thenApply(secondResult -> {
                if (secondResult.isValid()) {
                    return ValidationResult.Valid.INSTANCE;
                }
                // Both failed, combine reasons
                List<String> allReasons = new ArrayList<>();
                if (firstResult instanceof ValidationResult.Invalid invalid) {
                    invalid.errors().forEach(allReasons::add);
                }
                if (secondResult instanceof ValidationResult.Invalid invalid) {
                    invalid.errors().forEach(allReasons::add);
                }
                return new ValidationResult.Invalid(allReasons);
            });
        });
    }
}
