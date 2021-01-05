package ru.neosvet.chat.client.connect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ru.neosvet.chat.base.Const;
import ru.neosvet.chat.client.Client;

public class ConnectController {
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private Label statusLabel;

    private Client client;


    @FXML
    public void initialize() {
        hostField.setText(Const.DEFAULT_HOST);
        portField.setText(String.valueOf(Const.DEFAULT_PORT));
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setError(String msg) {
        statusLabel.setText("Connection to server failed: " + msg);
    }

    public void connect(ActionEvent actionEvent) {
        statusLabel.setText("Connection...");
        client.connect(hostField.getText(), Integer.parseInt(portField.getText()));
    }
}
