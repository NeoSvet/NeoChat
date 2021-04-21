package ru.neosvet.chat.client.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.neosvet.chat.base.Cmd;
import ru.neosvet.chat.base.RequestFactory;
import ru.neosvet.chat.client.Client;

import java.io.IOException;

public class AuthController {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label labelError;

    private Client client;

    @FXML
    public void signIn() {
        showError("");

        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Login or password is empty");
            return;
        }

        try {
            client.sendRequest(RequestFactory.createAuth(login, password));
        } catch (IOException e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    public void showError(String msg) {
        labelError.setText(msg);
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
