package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("sample.fxml")));
        primaryStage.setTitle("User Parser");
        primaryStage.setScene(new Scene(root, 600, 450));
        primaryStage.setResizable(false);
        primaryStage.show();
        Controller.set_primary_stage(primaryStage);
        primaryStage.setOnCloseRequest(Controller::close_window_event);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
