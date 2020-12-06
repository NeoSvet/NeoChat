package ru.neosvet.chat.client;

import ru.neosvet.chat.base.Cmd;
import ru.neosvet.chat.base.Const;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Network {
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private boolean connected = false;
    private Client client;
    private String nick = "noname";

    public Network(Client client) {
        this.client = client;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        connected = true;
    }

    public void close() {
        if (!connected)
            return;
        try {
            sendMessage(Cmd.EXIT);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitMessage() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String[] m = parseMessage(in.readUTF());
                    switch (m[0]) {
                        case Cmd.STOP:
                            connected = false;
                            socket.close();
                            client.showMessage("Server stopped");
                            return;
                        case Cmd.BYE:
                            connected = false;
                            socket.close();
                            client.showMessage("You left the chat");
                            return;
                        case Cmd.AUTH:
                            authentication(m);
                            break;
                        case Cmd.ERROR:
                            client.showErrorMessage(m[1], m[2]);
                            break;
                        case Cmd.JOIN:
                            client.joinUser(m[1]);
                            break;
                        case Cmd.LIST:
                            client.loadUserList(m);
                            break;
                        case Cmd.LEFT:
                            client.leftUser(m[1]);
                            break;
                        case Cmd.MSG_CLIENT:
                            client.showMessage(String.format("<%s>%s", m[1], m[2]));
                            break;
                        case Cmd.MSG_PRIVATE:
                            client.showMessage(String.format("[PRIVATE FROM]<%s>%s", m[1], m[2]));
                            break;
                        default:
                            client.showMessage(Arrays.toString(m));
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                client.showErrorMessage("Network", e.getMessage());
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    private void authentication(String[] m) {
        if (m[1].equals(Cmd.ERROR)) {
            client.resultAuth(m[2]);
            return;
        }
        nick = m[1];
        client.resultAuth(null);
    }

    private String[] parseMessage(String s) {
        return s.split(Const.SEPARATOR);
    }

    public void sendMessage(String msg) throws IOException {
        if (!connected) {
            client.showMessage("No connection");
            return;
        }
        out.writeUTF(msg);
        out.flush();
    }

    public void sendCommand(String cmd, String... args) throws IOException {
        if (cmd.equals(Cmd.AUTH))
            waitMessage();

        StringBuilder builder = new StringBuilder(cmd);
        for (String s : args) {
            builder.append(Const.SEPARATOR);
            builder.append(s);
        }
        sendMessage(builder.toString());
    }


    public String getNick() {
        return nick;
    }
}

