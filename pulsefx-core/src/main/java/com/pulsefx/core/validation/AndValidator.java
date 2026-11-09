package dev.yasmramos.pulsefx.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A validator that combines two validators using AND logic.
 * 
 * <p>Both validators must pass for the combined validator to pass.
 * If both fail, all error reasons from both are included.</p>
 * 
 * @param <T> the type of value to validate
 * @since 0.1.0
 */
final class AndValidator<T> implements Validator<T> {
    
    private final Validator<T> first;
    private final Validator<T> second;
    
    /**
     * Constructs a new AndValidator.
     * 
     * @param first the first validator
     * @param second the second validator
     */
    AndValidator(Validator<T> first, Validator<T> second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public ValidationResult<T> validate(T value) {
        ValidationResult<T> firstResult = first.validate(value);
        ValidationResult<T> secondResult = second.validate(value);
        
        if (firstResult.isValid() && secondResult.isValid()) {
            return ValidationResult.valid(null);
        }
        
        List<String> allReasons = new ArrayList<>();
        if (firstResult instanceof ValidationResult.Invalid<?> invalid) {
            invalid.errors().forEach(allReasons::add);
        }
        if (secondResult instanceof ValidationResult.Invalid<?> invalid) {
            invalid.errors().forEach(allReasons::add);
        }
        
        return new ValidationResult.Invalid<>(allReasons);
    }
    
    @Override
    public CompletableFuture<ValidationResult<T>> validateAsync(T value) {
        return first.validateAsync(value)
                .thenCompose(firstResult -> {
                    if (!firstResult.isValid()) {
                        // First failed, but we still need to check second for complete error reporting
                        return second.validateAsync(value).thenApply(secondResult -> {
                            List<String> allReasons = new ArrayList<>();
                            if (firstResult instanceof ValidationResult.Invalid<?> invalid) {
                                invalid.errors().forEach(allReasons::add);
                            }
                            if (secondResult instanceof ValidationResult.Invalid<?> invalid) {
                                invalid.errors().forEach(allReasons::add);
                            }
                            return new ValidationResult.Invalid<>(allReasons);
                        });
                    }
                    return second.validateAsync(value).thenApply(secondResult -> {
                        if (secondResult.isValid()) {
                            return ValidationResult.valid(null);
                        }
                        return secondResult;
                    });
                });
    }
}
