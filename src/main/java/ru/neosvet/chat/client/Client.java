package ru.neosvet.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestFactory;
import ru.neosvet.chat.base.requests.MessageRequest;
import ru.neosvet.chat.base.requests.RecordsRequest;
import ru.neosvet.chat.client.auth.AuthController;
import ru.neosvet.chat.client.chat.ChatController;
import ru.neosvet.chat.client.connect.ConnectController;

import java.io.IOException;

public class Client extends Application {
    private final String UI_CHAT = "/client/chat.fxml";
    private final String UI_AUTH = "/client/auth.fxml";
    private final String UI_CONNECT = "/client/connect.fxml";

    private ChatController chatCtrl;
    private AuthController authCtrl;
    private ConnectController connectCtrl;
    private Network network;
    private Stage authStage;
    private Stage chatStage;
    private Stage connectStage;

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
        chatCtrl = loader.getController();
        chatCtrl.setClient(this);

        primaryStage.setOnCloseRequest(windowEvent -> {
            network.close();
            chatCtrl.close();
        });
    }

    public void showErrorMessage(String title, String msg) {
        showMessage("[ERROR]" + title + ": " + msg);
    }

    public void sendMessage(String s) throws IOException {
        sendRequest(RequestFactory.createPublicMsg(network.getNick(), s));
    }

    public void connect(String host, int port) {
        network.connect(host, port);
    }

    public void putResultConnect(String error) {
        Platform.runLater(() -> {
            if (!connectStage.isShowing())
                return;

            if (error == null) {
                connectStage.close();
                openAuthWindow();
                return;
            }
            connectCtrl.setError(error);
        });
    }

    public void sendRequest(Request request) throws IOException {
        network.sendRequest(request);
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

            authCtrl = loader.getController();
            authCtrl.setClient(this);

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Connection to server failed: " + e.getMessage());
        }
    }

    public void resultAuth(String err_msg) {
        if (err_msg != null) {
            Platform.runLater(() -> {
                authCtrl.showError(err_msg);
            });
            return;
        }
        Platform.runLater(() -> {
            authStage.close();
            chatStage.setTitle("Chat: " + network.getNick());
            chatCtrl.setFocus();
        });
        showMessage("You connected as " + network.getNick());
    }

    public void showMessage(String msg) {
        Platform.runLater(() -> {
            chatCtrl.showMessage(msg);
        });
    }

    public void showMessage(String owner, String msg) {
        Platform.runLater(() -> {
            chatCtrl.showMessage(owner, msg);
        });
    }

    public String getMyNick() {
        return network.getNick();
    }

    public void joinUser(String nick) {
        showMessage(String.format("%s joined the chat", nick));
        Platform.runLater(() -> {
            chatCtrl.addUser(nick);
        });
    }

    public void leftUser(String nick) {
        showMessage(String.format("%s left the chat", nick));
        Platform.runLater(() -> {
            chatCtrl.removeUser(nick);
        });
    }

    public void loadUserList(String[] users) {
        Platform.runLater(() -> {
            for (int i = 0; i < users.length; i++) {
                chatCtrl.addUser(users[i]);
            }
        });
    }

    public void disconnected() {
        Platform.runLater(() -> {
            if (authStage.isShowing())
                authStage.close();
            chatStage.setTitle("Chat");
            chatCtrl.reset();
        });
    }

    public void renameUser(MessageRequest request) {
        Platform.runLater(() -> {
            chatCtrl.renameUser(request.getOwner(), request.getMsg());
        });
    }

    public void showRecords(RecordsRequest request) {
        Platform.runLater(() -> {
            chatCtrl.showRecords(request.getRecords(), "HISTORY FROM SERVER");
        });
    }

    public void openConnectWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(UI_CONNECT));
            Parent root = loader.load();

            connectStage = new Stage();

            connectStage.setTitle("Connection");
            connectStage.initModality(Modality.WINDOW_MODAL);
            connectStage.initOwner(chatStage);
            Scene scene = new Scene(root);
            connectStage.setScene(scene);
            connectStage.show();

            connectCtrl = loader.getController();
            connectCtrl.setClient(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnect() {
        return network.isConnect();
    }
}
