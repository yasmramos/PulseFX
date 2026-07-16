package com.pulsefx.core.validation;

import java.util.List;

/**
 * Result of a validation operation.
 * 
 * <p>This is a sealed interface with two possible outcomes: {@link Valid} or {@link Invalid}.
 * Use pattern matching to handle both cases:</p>
 * 
 * <pre>{@code
 * switch (result) {
 *     case Valid valid -> System.out.println("Validation passed");
 *     case Invalid invalid -> invalid.errors().forEach(System.out::println);
 * }
 * }</pre>
 * 
 * @since 0.1.0
 */
public sealed interface ValidationResult permits ValidationResult.Valid, ValidationResult.Invalid {
    
    /**
     * Returns true if this result represents a successful validation.
     * 
     * @return true if valid, false otherwise
     */
    boolean isValid();
    
    /**
     * Returns the list of error messages for this validation result.
     * For {@link Valid} results, this returns an empty list.
     * For {@link Invalid} results, this returns the list of failure reasons.
     * 
     * @return list of error messages (never null)
     * @since 0.1.0
     */
    List<String> errors();
    
    /**
     * A successful validation result.
     * 
     * @since 0.1.0
     */
    record Valid() implements ValidationResult {
        
        /**
         * The singleton instance for valid results.
         */
        public static final Valid INSTANCE = new Valid();
        
        @Override
        public boolean isValid() {
            return true;
        }
        
        @Override
        public List<String> errors() {
            return List.of();
        }
    }
    
    /**
     * A failed validation result with one or more error reasons.
     * 
     * @param errors non-empty list of validation failure reasons
     * @since 0.1.0
     */
    record Invalid(List<String> errors) implements ValidationResult {
        
        /**
         * Constructs a new Invalid result.
         * 
         * @param errors the list of validation failure reasons
         * @throws IllegalArgumentException if errors is null or empty
         */
        public Invalid {
            if (errors == null || errors.isEmpty()) {
                throw new IllegalArgumentException("Invalid result must have at least one reason");
            }
        }
        
        @Override
        public boolean isValid() {
            return false;
        }
    }
}
