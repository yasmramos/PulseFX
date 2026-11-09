package dev.yasmramos.pulsefx.navigation;

import dev.yasmramos.pulsefx.core.state.FormState;

/**
 * Functional interface for navigation guards.
 * Guards can prevent navigation based on custom logic.
 * <p>
 * Common use case: prevent navigation away from a view with unsaved form changes.
 *
 * @see NavigationGuards#formDirtyGuard(FormState)
 */
@FunctionalInterface
public interface NavigationGuard {

    /**
     * Evaluates whether navigation should be allowed.
     *
     * @param from the route being navigated from
     * @param to   the route being navigated to
     * @return true to allow navigation, false to block it
     */
    boolean canNavigate(Route from, Route to);
}
