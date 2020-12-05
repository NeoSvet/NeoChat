package ru.neosvet.chat.client.chat;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.neosvet.chat.Const;
import ru.neosvet.chat.client.Client;

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
    private ListView<String> lvUsers;

    private Client client;


    @FXML
    public void initialize() {
        initEventSelectUser();
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
        String msg = tfMessage.getText().trim();
        if (msg.isEmpty())
            return;
        /*if (msg.startsWith(Const.CMD_NICK)) {
            nick = msg.substring(msg.indexOf(" ") + 1);
            showMessage("Changed nick to " + nick);
            tfMessage.clear();
            return;
        }*/
        if (msg.equals(Const.CMD_CONNECT)) {
            connect(Const.DEFAULT_HOST, Const.DEFAULT_PORT);
            tfMessage.clear();
            return;
        }
        try {
            if (msg.equals(Const.CMD_EXIT)) {
                lvUsers.getItems().clear();
            } else {
                showMessage("<" + client.getMyNick() + ">" + msg);
            }
            sendMessage(msg);
            tfMessage.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMessage(String msg) {
        taChat.setText(taChat.getText() + getTime() + msg + "\n");
    }

    private String getTime() {
        return timeFormat.format(Calendar.getInstance().getTime());
    }


    public void addUser(String nick) {
        lvUsers.getItems().add(nick);
    }

    public void removeUser(String nick) {
        lvUsers.getItems().remove(nick);
        lvUsers.refresh();
    }

    public void connect(String localhost, int port) {
        try {
            client.connect(localhost, port);
            showMessage("Connection to server successful");
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Connection to server failed: " + e.getMessage());
        }
    }

    private void sendMessage(String msg) {
        try {
            client.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("[ERROR]" + e.getMessage());
        }
    }

    public void setClient(Client client) {
        this.client = client;
        connect(Const.DEFAULT_HOST, Const.DEFAULT_PORT);
    }
}
