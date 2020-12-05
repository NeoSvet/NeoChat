package ru.neosvet.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.neosvet.chat.client.auth.AuthController;
import ru.neosvet.chat.client.chat.ChatController;

import java.io.IOException;

public class Client extends Application {
    private final String UI_CHAT = "chat/chat.fxml";
    private final String UI_AUTH = "auth/auth.fxml";

    private ChatController chat;
    private AuthController auth;
    private Network network;
    private Stage authStage;
    private Stage chatStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(UI_CHAT));
        Parent root = loader.load();

        chatStage = primaryStage;
        chatStage.setTitle("Chat");
        chatStage.setScene(new Scene(root));
        chatStage.show();

        network = new Network(this);
        chat = loader.getController();
        chat.setClient(this);

        primaryStage.setOnCloseRequest(windowEvent -> network.close());
    }

    public void showErrorMessage(String title, String msg) {
        showMessage(title + ": " + msg);
    }

    public void sendMessage(String msg) throws IOException {
        network.sendMessage(msg);
    }

    public void connect(String host, int port) throws IOException {
        network.connect(host, port);
        openAuthWindow();
    }

    public void sendCommand(String cmd, String... args) throws IOException {
        network.sendCommand(cmd, args);
    }

    private void openAuthWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(UI_AUTH));
            Parent root = loader.load();

            authStage = new Stage();

            authStage.setTitle("Authentication");
            authStage.initModality(Modality.WINDOW_MODAL);
            authStage.initOwner(chatStage);
            Scene scene = new Scene(root);
            authStage.setScene(scene);
            authStage.show();

            auth = loader.getController();
            auth.setClient(this);

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Connection to server failed: " + e.getMessage());
        }
    }

    public void resultAuth(String msg) {
        if (msg != null) {
            Platform.runLater(() -> {
                auth.showError(msg);
            });
        } else {
            Platform.runLater(() -> {
                authStage.close();
            });
            showMessage("You connected as " + network.getNick());
        }
    }

    public void showMessage(String msg) {
        Platform.runLater(() -> {
            chat.showMessage(msg);
        });
    }

    public String getMyNick() {
        return network.getNick();
    }
}
