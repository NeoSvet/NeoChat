package ru.neosvet.chat.client.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.neosvet.chat.base.RequestFactory;
import ru.neosvet.chat.client.Client;

import java.io.IOException;

public class AuthController {
    @FXML
    public TextField tflogin, tfNick;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label lError, lNick;
    @FXML
    public Button bSwitcher, bSignIn, bRegister;

    private Client client;

    @FXML
    public void signIn() {
        showError("");

        String login = tflogin.getText();
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
        lError.setText(msg);
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    public void goSwitch() {
        if (bSignIn.isVisible()) {
            bSwitcher.setText("Login");
            bSignIn.setVisible(false);
            bRegister.setVisible(true);
            lNick.setVisible(true);
            tfNick.setVisible(true);
        } else {
            bSwitcher.setText("Registration");
            bSignIn.setVisible(true);
            bRegister.setVisible(false);
            lNick.setVisible(false);
            tfNick.setVisible(false);
        }
    }

    @FXML
    public void register() {
        showError("");

        String login = tflogin.getText();
        String password = passwordField.getText();
        String nick = tfNick.getText();

        if (login.isEmpty() || password.isEmpty() || nick.isEmpty()) {
            showError("Login or password or nick is empty");
            return;
        }

        try {
            client.sendRequest(RequestFactory.createReg(login, password, nick));
        } catch (IOException e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }
}
