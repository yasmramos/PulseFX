package dev.yasmramos.pulsefx.navigation;

/**
 * Abstract base class for routes that need custom behavior.
 * Most routes should be defined as simple records implementing {@link Route}.
 */
public abstract class AbstractRoute implements Route {
    
    /**
     * Returns the route identifier for matching purposes.
     * By default, uses the simple class name.
     *
     * @return the route identifier
     */
    @Override
    public String getRouteId() {
        return this.getClass().getSimpleName();
    }
}
