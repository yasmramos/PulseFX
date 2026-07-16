package dev.yasmramos.pulsefx.navigation;

import javafx.scene.Node;

/**
 * Interface for view components that participate in the navigation lifecycle.
 * Implement this interface in your JavaFX Node subclasses to receive lifecycle callbacks.
 */
public interface ViewComponent {

    /**
     * Called when the user navigates to this view.
     * Use this method to initialize view state with route parameters.
     *
     * @param route the route that triggered this navigation
     */
    void onEnter(Route route);

    /**
     * Called when the user navigates away from this view.
     * Use this method to clean up resources or save state.
     *
     * @param nextRoute the route being navigated to
     */
    void onExit(Route nextRoute);

    /**
     * Called when the user navigates back from this view.
     * Return false to prevent the back navigation (e.g., if there are unsaved changes).
     *
     * @param previousRoute the route being navigated back to
     * @return true to allow back navigation, false to cancel it
     */
    default boolean onBack(Route previousRoute) {
        return true;
    }

    /**
     * Returns the root JavaFX node for this view component.
     *
     * @return the root node
     */
    Node getRoot();
}
