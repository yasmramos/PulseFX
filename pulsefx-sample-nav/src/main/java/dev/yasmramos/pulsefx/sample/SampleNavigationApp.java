package dev.yasmramos.pulsefx.sample;

import dev.yasmramos.pulsefx.core.state.FormState;
import dev.yasmramos.pulsefx.core.validation.Rules;
import dev.yasmramos.pulsefx.navigation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.UUID;

/**
 * Sample application demonstrating PulseFX Navigation features.
 */
public class SampleNavigationApp extends Application {

    private Router router;
    private ViewContainer container;
    private Button backButton;
    private Button forwardButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create view container
        container = new ViewContainer();
        
        // Create navigation bar
        BorderPane root = new BorderPane();
        root.setCenter(container);
        root.setTop(createNavigationBar());
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("PulseFX Navigation Sample");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initialize router
        router = new Router(container);
        router.setTransitionType(TransitionType.FADE);
        
        // Register route handlers
        router.addRouteHandler(HomeRoute.class, HomeView::new);
        router.addRouteHandler(ProductListRoute.class, ProductListView::new);
        router.addRouteHandler(ProductDetailRoute.class, ProductDetailView::new);
        router.addRouteHandler(EditProductRoute.class, EditProductView::new);
        
        // Add form dirty guard for edit view
        router.addGuard((from, to) -> {
            if (from instanceof EditProductRoute edit && !edit.isSaved()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes");
                alert.setContentText("Are you sure you want to leave?");
                return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
            }
            return true;
        });
        
        // Navigate to home
        router.goTo(new HomeRoute());
        
        // Bind navigation buttons
        backButton.disableProperty().bind(router.canGoBackProperty().not());
        forwardButton.disableProperty().bind(router.canGoForwardProperty().not());
    }

    private HBox createNavigationBar() {
        backButton = new Button("← Back");
        forwardButton = new Button("Forward →");
        
        backButton.setOnAction(e -> router.back());
        forwardButton.setOnAction(e -> router.forward());
        
        HBox navBar = new HBox(10, backButton, forwardButton);
        navBar.setPadding(new Insets(10));
        navBar.setAlignment(Pos.CENTER_LEFT);
        return navBar;
    }

    // Route definitions
    public record HomeRoute() implements Route {}
    public record ProductListRoute() implements Route {}
    public record ProductDetailRoute(UUID productId) implements Route {}
    public record EditProductRoute(UUID productId, boolean isSaved) implements Route {}

    // View implementations
    public static class HomeView extends VBox implements ViewComponent {
        private final Button productListBtn;
        
        public HomeView() {
            setAlignment(Pos.CENTER);
            setSpacing(20);
            
            Label title = new Label("Home");
            title.setStyle("-fx-font-size: 24px;");
            
            productListBtn = new Button("View Products");
            
            getChildren().addAll(title, productListBtn);
        }
        
        @Override
        public void onEnter(Route route) {
            System.out.println("Entering Home");
        }
        
        @Override
        public void onExit(Route nextRoute) {
            System.out.println("Exiting Home");
        }
        
        @Override
        public Node getRoot() {
            return this;
        }
    }

    public static class ProductListView extends VBox implements ViewComponent {
        public ProductListView() {
            setAlignment(Pos.CENTER);
            setSpacing(20);
            
            Label title = new Label("Product List");
            title.setStyle("-fx-font-size: 24px;");
            
            getChildren().add(title);
        }
        
        @Override
        public void onEnter(Route route) {}
        @Override
        public void onExit(Route nextRoute) {}
        @Override
        public Node getRoot() { return this; }
    }

    public static class ProductDetailView extends VBox implements ViewComponent {
        public ProductDetailView() {
            setAlignment(Pos.CENTER);
            setSpacing(20);
            
            Label title = new Label("Product Detail");
            title.setStyle("-fx-font-size: 24px;");
            
            getChildren().add(title);
        }
        
        @Override
        public void onEnter(Route route) {}
        @Override
        public void onExit(Route nextRoute) {}
        @Override
        public Node getRoot() { return this; }
    }

    public static class EditProductView extends VBox implements ViewComponent {
        private final TextField nameField;
        private final FormState formState;
        private boolean saved = false;
        
        public EditProductView() {
            setAlignment(Pos.CENTER);
            setSpacing(20);
            
            Label title = new Label("Edit Product");
            title.setStyle("-fx-font-size: 24px;");
            
            nameField = new TextField();
            nameField.setPromptText("Product Name");
            
            formState = FormState.builder()
                .field("name", nameField.textProperty(), Rules.nonEmpty())
                .build();
            
            Button saveBtn = new Button("Save");
            saveBtn.disableProperty().bind(formState.validProperty().not());
            saveBtn.setOnAction(e -> {
                saved = true;
                System.out.println("Product saved!");
            });
            
            getChildren().addAll(title, nameField, saveBtn);
        }
        
        @Override
        public void onEnter(Route route) {
            if (route instanceof EditProductRoute edit) {
                System.out.println("Editing product: " + edit.productId());
            }
        }
        
        @Override
        public void onExit(Route nextRoute) {}
        
        public boolean isSaved() {
            return saved;
        }
        
        @Override
        public Node getRoot() { return this; }
    }
}
