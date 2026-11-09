package dev.yasmramos.pulsefx.navigation;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Supplier;

/**
 * Type-safe router with back stack support and view lifecycle management.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Type-safe routes using records</li>
 *   <li>Observable back/forward state for UI binding</li>
 *   <li>View lifecycle callbacks (onEnter, onExit, onBack)</li>
 *   <li>Navigation guards integration</li>
 *   <li>Configurable transitions (fade, slide)</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * record ProductRoute(UUID productId) implements Route {}
 * 
 * Router router = new Router(viewContainer);
 * router.addRouteHandler(ProductRoute.class, route -> new ProductView(route.productId()));
 * router.goTo(new ProductRoute(UUID.randomUUID()));
 * }</pre>
 */
public class Router {

    private final ViewContainer container;
    private final Deque<Route> backStack;
    private final Deque<Route> forwardStack;
    private final Map<Class<? extends Route>, Supplier<ViewComponent>> routeHandlers;
    private final ObservableList<NavigationGuard> guards;
    
    private final ObjectProperty<Route> currentRouteProperty;
    private final BooleanProperty canGoBackProperty;
    private final BooleanProperty canGoForwardProperty;
    private final ObjectProperty<TransitionType> transitionTypeProperty;
    private final ObjectProperty<Duration> transitionDurationProperty;

    /**
     * Creates a new router with the specified view container.
     *
     * @param container the container where views will be displayed
     */
    public Router(ViewContainer container) {
        this.container = Objects.requireNonNull(container, "ViewContainer cannot be null");
        this.backStack = new ArrayDeque<>();
        this.forwardStack = new ArrayDeque<>();
        this.routeHandlers = new HashMap<>();
        this.guards = FXCollections.observableArrayList();
        
        this.currentRouteProperty = new SimpleObjectProperty<>();
        this.canGoBackProperty = new SimpleBooleanProperty(false);
        this.canGoForwardProperty = new SimpleBooleanProperty(false);
        this.transitionTypeProperty = new SimpleObjectProperty<>(TransitionType.FADE);
        this.transitionDurationProperty = new SimpleObjectProperty<>(Duration.millis(300));
        
        updateNavigationState();
    }

    /**
     * Registers a handler for a specific route type.
     *
     * @param routeClass the route class to handle
     * @param handler    a supplier that creates the view component for this route
     * @param <R>        the route type
     * @return this router for chaining
     */
    public <R extends Route> Router addRouteHandler(Class<R> routeClass, Supplier<ViewComponent> handler) {
        routeHandlers.put(routeClass, handler);
        return this;
    }

    /**
     * Adds a navigation guard that will be evaluated before each navigation.
     *
     * @param guard the guard to add
     * @return this router for chaining
     */
    public Router addGuard(NavigationGuard guard) {
        guards.add(guard);
        return this;
    }

    /**
     * Removes a navigation guard.
     *
     * @param guard the guard to remove
     * @return this router for chaining
     */
    public Router removeGuard(NavigationGuard guard) {
        guards.remove(guard);
        return this;
    }

    /**
     * Navigates to the specified route.
     *
     * @param route the route to navigate to
     * @return true if navigation was successful, false if blocked by a guard
     */
    public boolean goTo(Route route) {
        Objects.requireNonNull(route, "Route cannot be null");
        
        // Check guards
        Route current = currentRouteProperty.get();
        for (NavigationGuard guard : guards) {
            if (!guard.canNavigate(current, route)) {
                return false;
            }
        }

        // Get current view component for lifecycle callbacks
        ViewComponent currentView = getCurrentViewComponent();
        
        // Handle back navigation from current view
        if (currentView != null && !currentView.onBack(route)) {
            return false;
        }

        // Execute exit callback on current view
        if (currentView != null) {
            currentView.onExit(route);
        }

        // Push current route to back stack
        if (current != null) {
            backStack.push(current);
        }
        
        // Clear forward stack on new navigation
        forwardStack.clear();

        // Create new view component
        ViewComponent newView = createViewForRoute(route);
        if (newView == null) {
            throw new IllegalStateException("No handler registered for route: " + route.getClass().getName());
        }

        // Execute enter callback on new view
        newView.onEnter(route);

        // Perform transition
        Node currentNode = currentView != null ? currentView.getRoot() : null;
        Node newNode = newView.getRoot();
        performTransition(currentNode, newNode);

        // Update state
        currentRouteProperty.set(route);
        updateNavigationState();

        return true;
    }

    /**
     * Navigates back to the previous route.
     *
     * @return true if back navigation was successful, false if no history or blocked
     */
    public boolean back() {
        if (backStack.isEmpty()) {
            return false;
        }

        Route current = currentRouteProperty.get();
        Route previous = backStack.pop();
        
        // Check if current view allows back navigation
        ViewComponent currentView = getCurrentViewComponent();
        if (currentView != null && !currentView.onBack(previous)) {
            backStack.push(previous); // Restore back stack
            return false;
        }

        // Execute exit callback
        if (currentView != null) {
            currentView.onExit(previous);
        }

        // Push current to forward stack
        forwardStack.push(current);

        // Create previous view
        ViewComponent previousView = createViewForRoute(previous);
        if (previousView == null) {
            throw new IllegalStateException("No handler registered for route: " + previous.getClass().getName());
        }

        previousView.onEnter(previous);

        // Perform transition
        Node currentNode = currentView != null ? currentView.getRoot() : null;
        Node newNode = previousView.getRoot();
        performTransition(currentNode, newNode);

        currentRouteProperty.set(previous);
        updateNavigationState();

        return true;
    }

    /**
     * Navigates forward to the next route (after a back navigation).
     *
     * @return true if forward navigation was successful, false if no forward history
     */
    public boolean forward() {
        if (forwardStack.isEmpty()) {
            return false;
        }

        Route current = currentRouteProperty.get();
        Route next = forwardStack.pop();

        // Check guards
        for (NavigationGuard guard : guards) {
            if (!guard.canNavigate(current, next)) {
                forwardStack.push(next); // Restore forward stack
                return false;
            }
        }

        ViewComponent currentView = getCurrentViewComponent();
        
        if (currentView != null && !currentView.onBack(next)) {
            forwardStack.push(next);
            return false;
        }

        if (currentView != null) {
            currentView.onExit(next);
        }

        backStack.push(current);

        ViewComponent nextView = createViewForRoute(next);
        if (nextView == null) {
            throw new IllegalStateException("No handler registered for route: " + next.getClass().getName());
        }

        nextView.onEnter(next);

        Node currentNode = currentView != null ? currentView.getRoot() : null;
        Node newNode = nextView.getRoot();
        performTransition(currentNode, newNode);

        currentRouteProperty.set(next);
        updateNavigationState();

        return true;
    }

    /**
     * Returns an observable property indicating if back navigation is possible.
     *
     * @return observable boolean property
     */
    public BooleanBinding canGoBackProperty() {
        return canGoBackProperty;
    }

    /**
     * Returns an observable property indicating if forward navigation is possible.
     *
     * @return observable boolean property
     */
    public BooleanBinding canGoForwardProperty() {
        return canGoForwardProperty;
    }

    /**
     * Returns the current route.
     *
     * @return the current route, or null if no route is active
     */
    public Route getCurrentRoute() {
        return currentRouteProperty.get();
    }

    /**
     * Returns an observable property of the current route.
     *
     * @return observable object property
     */
    public ReadOnlyObjectProperty<Route> currentRouteProperty() {
        return currentRouteProperty;
    }

    /**
     * Sets the transition type for navigation animations.
     *
     * @param transitionType the transition type
     */
    public void setTransitionType(TransitionType transitionType) {
        transitionTypeProperty.set(transitionType);
    }

    /**
     * Returns the current transition type.
     *
     * @return the transition type
     */
    public TransitionType getTransitionType() {
        return transitionTypeProperty.get();
    }

    /**
     * Sets the duration for navigation transitions.
     *
     * @param duration the transition duration
     */
    public void setTransitionDuration(Duration duration) {
        transitionDurationProperty.set(duration);
    }

    /**
     * Returns the current transition duration.
     *
     * @return the transition duration
     */
    public Duration getTransitionDuration() {
        return transitionDurationProperty.get();
    }

    private ViewComponent getCurrentViewComponent() {
        return container.getCurrentView();
    }

    private ViewComponent createViewForRoute(Route route) {
        Supplier<ViewComponent> handler = routeHandlers.get(route.getClass());
        if (handler == null) {
            // Try to find a handler for a superclass/superinterface
            for (Map.Entry<Class<? extends Route>, Supplier<ViewComponent>> entry : routeHandlers.entrySet()) {
                if (entry.getKey().isInstance(route)) {
                    handler = entry.getValue();
                    break;
                }
            }
        }
        return handler != null ? handler.get() : null;
    }

    private void performTransition(Node from, Node to) {
        container.setView(to);
        
        if (from == null || to == null) {
            return;
        }

        TransitionType type = transitionTypeProperty.get();
        Duration duration = transitionDurationProperty.get();

        switch (type) {
            case FADE:
                performFadeTransition(from, to, duration);
                break;
            case SLIDE:
                performSlideTransition(from, to, duration);
                break;
            case NONE:
            default:
                // No animation
                break;
        }
    }

    private void performFadeTransition(Node from, Node to, Duration duration) {
        from.setOpacity(1.0);
        to.setOpacity(0.0);

        FadeTransition fadeOut = new FadeTransition(duration, from);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(duration, to);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(fadeOut, fadeIn);
        parallel.play();
    }

    private void performSlideTransition(Node from, Node to, Duration duration) {
        to.setTranslateX(container.getWidth());
        
        TranslateTransition slideOut = new TranslateTransition(duration, from);
        slideOut.setFromX(0);
        slideOut.setToX(-container.getWidth());

        TranslateTransition slideIn = new TranslateTransition(duration, to);
        slideIn.setFromX(container.getWidth());
        slideIn.setToX(0);

        ParallelTransition parallel = new ParallelTransition(slideOut, slideIn);
        parallel.play();
    }

    private void updateNavigationState() {
        canGoBackProperty.set(!backStack.isEmpty());
        canGoForwardProperty.set(!forwardStack.isEmpty());
    }
}
