package ru.neosvet.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
    private final String PATH_TO_UI = "chat.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(PATH_TO_UI));
        Parent root = loader.load();

        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        ChatController ChatController = loader.getController();

        primaryStage.setOnCloseRequest(windowEvent -> ChatController.close());
    }

}
