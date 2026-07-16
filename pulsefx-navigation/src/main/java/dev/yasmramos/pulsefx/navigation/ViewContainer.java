package dev.yasmramos.pulsefx.navigation;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * A JavaFX container that holds the currently active view.
 * This class wraps a StackPane and provides methods for the Router to manage views.
 */
public class ViewContainer extends StackPane {

    private ViewComponent currentView;

    /**
     * Creates a new view container.
     */
    public ViewContainer() {
        super();
    }

    /**
     * Sets the current view in this container.
     *
     * @param node the node to display, or null to clear the container
     */
    public void setView(Node node) {
        getChildren().clear();
        if (node != null) {
            getChildren().add(node);
        }
    }

    /**
     * Returns the current view component.
     *
     * @return the current view component, or null if no view is active
     */
    public ViewComponent getCurrentView() {
        return currentView;
    }

    /**
     * Sets the current view component and displays its root node.
     *
     * @param view the view component to set
     */
    public void setCurrentView(ViewComponent view) {
        this.currentView = view;
        if (view != null) {
            setView(view.getRoot());
        } else {
            setView(null);
        }
    }
}
