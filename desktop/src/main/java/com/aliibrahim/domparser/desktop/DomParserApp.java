package com.aliibrahim.domparser.desktop;

import com.aliibrahim.domparser.desktop.feature.Feature;
import com.aliibrahim.domparser.desktop.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/** Wires services, the main view and all {@link Feature}s together. */
public class DomParserApp extends Application {

    public static final String STYLESHEET =
            Objects.requireNonNull(DomParserApp.class.getResource("app.css")).toExternalForm();

    private AppServices services;

    @Override
    public void init() {
        services = AppServices.createDefault();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(DomParserApp.class.getResource("main-view.fxml"));
        MainController controller = new MainController(services);
        loader.setController(controller);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().add(STYLESHEET);
        stage.setScene(scene);
        stage.setMinWidth(920);
        stage.setMinHeight(560);

        List<Feature> features = ServiceLoader.load(Feature.class).stream().map(ServiceLoader.Provider::get).toList();
        controller.attach(stage, features);

        stage.show();
        getParameters().getUnnamed().stream().findFirst().map(Path::of).ifPresent(controller::open);
    }

    @Override
    public void stop() {
        services.background().shutdownNow();
    }
}
