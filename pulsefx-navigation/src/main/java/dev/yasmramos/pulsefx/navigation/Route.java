package dev.yasmramos.pulsefx.navigation;

/**
 * Marker interface for all route types.
 * Routes are defined as records with type-safe parameters.
 * <p>
 * Example:
 * {@code record ProductRoute(UUID productId) implements Route {}}
 *
 * @see Router#goTo(Route)
 */
public sealed interface Route permits AbstractRoute {
    
    /**
     * Returns the route identifier for matching purposes.
     * By default, uses the simple class name.
     *
     * @return the route identifier
     */
    default String getRouteId() {
        return this.getClass().getSimpleName();
    }
}
