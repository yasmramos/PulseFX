/**
 * PulseFX Core module - State Management and Validation Layer.
 * 
 * <p>This module provides reactive state management and validation capabilities
 * for JavaFX applications. It includes:</p>
 * <ul>
 *   <li>Reactive validation engine with sync/async support</li>
 *   <li>Form state management with dirty tracking</li>
 *   <li>Composable validation rules</li>
 * </ul>
 * 
 * @since 0.1.0
 */
module com.pulsefx.core {
    requires javafx.base;
    requires javafx.controls;
    
    exports com.pulsefx.core.validation;
    exports com.pulsefx.core.form;
    exports com.pulsefx.core.rules;
}
