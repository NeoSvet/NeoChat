package ru.neosvet.chat.client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.neosvet.chat.Const;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChatController {
    private SimpleDateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss]");
    @FXML
    private TextField tfMessage;
    @FXML
    private TextArea taChat;
    @FXML
    private ListView lvUsers;

    private String nick = "noname";
    private Network network;

    @FXML
    public void initialize() {
        initEventSelectUser();
        connect(Const.DEFAULT_HOST, Const.DEFAULT_PORT);
    }

    private void initEventSelectUser() {
        lvUsers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

            }
        });
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        if (tfMessage.getText().isEmpty())
            return;
        String msg = tfMessage.getText().trim();
        if (msg.isEmpty())
            return;
        if (msg.startsWith(Const.CMD_NICK)) {
            nick = msg.substring(msg.indexOf(" ") + 1);
            showMessage("Changed nick to " + nick);
            tfMessage.clear();
            return;
        }
        if (msg.equals(Const.CMD_CONNECT)) {
            sendMessage(msg);
            tfMessage.clear();
            return;
        }
        msg = "<" + nick + ">" + msg;
        try {
            showMessage(msg);
            sendMessage(msg);
            tfMessage.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMessage(String msg) {
        if (taChat.getText().isEmpty())
            taChat.setText(getTime() + msg + "\n");
        else
            taChat.setText(taChat.getText() + getTime() + msg + "\n");
    }

    private String getTime() {
        return timeFormat.format(Calendar.getInstance().getTime());
    }

    public void addUser(String name) {
        lvUsers.getItems().add(name);
    }

    public String getNick() {
        return nick;
    }

    public void close() {
        network.close(nick);
    }

    public void connect(String localhost, int port) {
        network = new Network(this);
        try {
            network.connect(localhost, port);
            showMessage("Connection to server successful");
            network.waitMessage();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Connection to server failed: " + e.getMessage());
        }
    }

    private void sendMessage(String msg) {
        if(msg.equals(Const.CMD_CONNECT)) {
            connect(Const.DEFAULT_HOST, Const.DEFAULT_PORT);
            return;
        }
        try {
            network.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage(e.getMessage());
        }
    }

    public void showErrorMessage(String msg) {
        showMessage("Error: " + msg);
    }
}
