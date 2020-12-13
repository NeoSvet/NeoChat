package ru.neosvet.chat.client.chat;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.neosvet.chat.base.Cmd;
import ru.neosvet.chat.base.Const;
import ru.neosvet.chat.base.RequestParser;
import ru.neosvet.chat.base.RequestType;
import ru.neosvet.chat.base.requests.PrivateMessageRequest;
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
    private Label lPrivate;
    @FXML
    private ListView<String> lvUsers;

    private final String SEND_PUBLIC = "Send public message";
    private Client client;
    private String selectedUser = null;


    @FXML
    public void initialize() {
        initEventSelectUser();
        lPrivate.setText(SEND_PUBLIC);
    }

    private void initEventSelectUser() {
        lvUsers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                selectedUser = newValue;
                lPrivate.setText("Send private message to " + selectedUser);
            }
        });
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        String msg = tfMessage.getText().trim();
        if (msg.isEmpty())
            return;
        if (msg.equals(Cmd.CONNECT)) {
            connect(Const.DEFAULT_HOST, Const.DEFAULT_PORT);
            tfMessage.clear();
            return;
        }
        try {
            sendMessage(msg);
            tfMessage.clear();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("[ERROR]" + e.getMessage());
        }
    }

    public void showMessage(String msg) {
        taChat.appendText(getTime() + msg + "\n");
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

    private void sendMessage(String msg) throws IOException {
        if (selectedUser != null && !msg.startsWith("/")) {
            msg = Cmd.MSG_PRIVATE + " " + selectedUser + " " + msg;
        }
        RequestParser parser = new RequestParser(client.getMyNick());
        if (parser.parse(msg)) {
            client.sendRequest(parser.getResult());
            if (parser.getResult().getType() == RequestType.MSG_PRIVATE) {
                PrivateMessageRequest pmr = (PrivateMessageRequest) parser.getResult();
                showMessage(String.format("[PRIVATE TO]<%s>%s", pmr.getRecipient(), pmr.getMsg()));
                return;
            } else if (parser.getResult().getType() == RequestType.EXIT) {
                return;
            }
        } else {
            client.sendMessage(msg);
        }
        showMessage(String.format("<%s>%s", client.getMyNick(), msg));
    }

    public void setClient(Client client) {
        this.client = client;
        connect(Const.DEFAULT_HOST, Const.DEFAULT_PORT);
    }

    public void unSelectUser(ActionEvent actionEvent) {
        selectedUser = null;
        lvUsers.getSelectionModel().clearSelection();
        lPrivate.setText(SEND_PUBLIC);
    }

    public void reset() {
        lvUsers.getItems().clear();
    }

    public void renameUser(String old_nick, String new_nick) {
        for (int i = 0; i < lvUsers.getItems().size(); i++) {
            if (lvUsers.getItems().get(i).equals(old_nick)) {
                lvUsers.getItems().set(i, new_nick);
                break;
            }
        }
        showMessage(String.format("User %s renamed to %s", old_nick, new_nick));
    }

    public void setFocus() {
        System.out.println("set focus");
        tfMessage.requestFocus();
    }
}
