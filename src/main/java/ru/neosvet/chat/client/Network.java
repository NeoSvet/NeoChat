package ru.neosvet.chat.client;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestFactory;
import ru.neosvet.chat.base.requests.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Network {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private boolean connected = false;
    private Client client;
    private String nick = null;

    public Network(Client client) {
        this.client = client;
    }

    public void connect(String host, int port) {
        Thread thread = new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                connected = true;
                client.putResultConnect(null);
                waitMessage();
            } catch (IOException e) {
                e.printStackTrace();
                client.putResultConnect(e.getMessage());
            }
        });
        thread.start();
    }

    public void close() {
        if (!connected)
            return;
        try {
            sendRequest(RequestFactory.createExit());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitMessage() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    Request request = readRequest();
                    if (request == null)
                        continue;

                    switch (request.getType()) {
                        case STOP:
                            reset();
                            client.showMessage("Server stopped");
                            return;
                        case BYE:
                            reset();
                            client.showMessage("You left the chat");
                            return;
                        case KICK:
                            reset();
                            client.showMessage("You was kicked");
                            break;
                        case NICK:
                            setNick(getNickFromRequest(request));
                            break;
                        case ERROR:
                            MessageRequest mrErr = (MessageRequest) request;
                            if (nick == null) //if user is not auth
                                client.resultAuth(mrErr.getMsg());
                            else
                                client.showErrorMessage(mrErr.getOwner(), mrErr.getMsg());
                            break;
                        case JOIN:
                            client.joinUser(getNickFromRequest(request));
                            break;
                        case LIST:
                            client.loadUserList(((ListRequest) request).getUsers());
                            break;
                        case LEFT:
                            client.leftUser(getNickFromRequest(request));
                            break;
                        case RENAME:
                            client.renameUser((MessageRequest) request);
                            break;
                        case MSG_PUBLIC:
                            MessageRequest mr = (MessageRequest) request;
                            client.showMessage(mr.getOwner(), mr.getMsg());
                            break;
                        case MSG_PRIVATE:
                            PrivateMessageRequest pmr = (PrivateMessageRequest) request;
                            client.showMessage(pmr.getSender(), "[PRIVATE]" + pmr.getMsg());
                            break;
                        case RECORDS:
                            client.showRecords((RecordsRequest) request);
                            break;
                        default:
                            client.showMessage("[ERROR]Unknown request: " + request.getType());
                            break;
                    }
                }
            } catch (IOException e) {
                if (!connected)
                    return;
                e.printStackTrace();
                client.showErrorMessage("Network", e.getMessage());
                client.disconnected();
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    private void reset() throws IOException {
        connected = false;
        socket.close();
        nick = null;
        client.disconnected();
    }

    private String getNickFromRequest(Request request) {
        return ((UserRequest) request).getNick();
    }

    private Request readRequest() throws IOException {
        try {
            return (Request) in.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR]Unknown request");
            e.printStackTrace();
            return null;
        }
    }

    private void setNick(String nick) {
        this.nick = nick;
        client.resultAuth(null);
    }

    public void sendRequest(Request request) throws IOException {
        if (!connected) {
            client.showMessage("No connection");
            return;
        }

        out.writeObject(request);
        out.flush();
    }

    public String getNick() {
        return nick;
    }

    public boolean isConnect() {
        return connected;
    }
}

