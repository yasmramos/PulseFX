package dev.yasmramos.pulsefx.navigation;

import dev.yasmramos.pulsefx.core.state.FormState;

import java.util.Objects;

/**
 * Utility class for creating common navigation guards.
 */
public final class NavigationGuards {

    private NavigationGuards() {
        // Utility class
    }

    /**
     * Creates a guard that prevents navigation if the form is dirty (has unsaved changes).
     * When blocked, the user should be prompted to confirm or cancel the navigation.
     *
     * @param formState the form state to check
     * @return a navigation guard that blocks when form is dirty
     */
    public static NavigationGuard formDirtyGuard(FormState formState) {
        Objects.requireNonNull(formState, "FormState cannot be null");
        return (from, to) -> !formState.isDirty();
    }

    /**
     * Creates a guard that always allows navigation.
     *
     * @return a permissive navigation guard
     */
    public static NavigationGuard allowAll() {
        return (from, to) -> true;
    }

    /**
     * Creates a guard that always blocks navigation.
     *
     * @return a restrictive navigation guard
     */
    public static NavigationGuard denyAll() {
        return (from, to) -> false;
    }

    /**
     * Combines multiple guards with AND logic. All guards must allow navigation.
     *
     * @param guards the guards to combine
     * @return a combined guard
     */
    public static NavigationGuard allOf(NavigationGuard... guards) {
        Objects.requireNonNull(guards, "Guards array cannot be null");
        return (from, to) -> {
            for (NavigationGuard guard : guards) {
                if (!guard.canNavigate(from, to)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Combines multiple guards with OR logic. At least one guard must allow navigation.
     *
     * @param guards the guards to combine
     * @return a combined guard
     */
    public static NavigationGuard anyOf(NavigationGuard... guards) {
        Objects.requireNonNull(guards, "Guards array cannot be null");
        return (from, to) -> {
            for (NavigationGuard guard : guards) {
                if (guard.canNavigate(from, to)) {
                    return true;
                }
            }
            return false;
        };
    }
}
