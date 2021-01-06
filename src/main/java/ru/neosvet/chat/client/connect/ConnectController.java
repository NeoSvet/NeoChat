package ru.neosvet.chat.client.connect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ru.neosvet.chat.client.Client;

public class ConnectController {
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private Label statusLabel;

    private Client client;

    public void setClient(Client client) {
        this.client = client;
        hostField.setText(client.getSettings().getHost());
        portField.setText(client.getSettings().getPort());
    }

    public void setError(String msg) {
        statusLabel.setText("Connection to server failed: " + msg);
    }

    public void connect(ActionEvent actionEvent) {
        statusLabel.setText("Connection...");
        client.getSettings().setHost(hostField.getText());
        client.getSettings().setPort(portField.getText());
        client.connect(hostField.getText(), Integer.parseInt(portField.getText()));
    }
}
