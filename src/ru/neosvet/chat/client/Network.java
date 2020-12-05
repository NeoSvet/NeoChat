package ru.neosvet.chat.client;

import ru.neosvet.chat.Const;

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
            sendMessage(Const.CMD_EXIT);
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
                        case Const.CMD_STOP:
                            connected = false;
                            client.showMessage("Connection interrupted");
                            return;
                        case Const.CMD_AUTH:
                            authentication(m);
                            break;
                        case Const.CMD_ERROR:
                            client.showErrorMessage(m[1], m[2]);
                            break;
                        case Const.MSG_CLIENT:
                            client.showMessage(String.format("<%s>%s", m[1], m[2]));
                            break;
                        case Const.MSG_PRIVATE:
                            client.showMessage(String.format("[PRIVATE]<%s>%s", m[1], m[2]));
                            break;
                        case Const.MSG_SYSTEM:
                            client.showMessage("[SYSTEM]" + m[1]);
                            break;
                        default:
                            client.showMessage(Arrays.toString(m));
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                client.showErrorMessage("Network error", e.getMessage());
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    private void authentication(String[] m) {
        if (m[1].equals(Const.CMD_ERROR)) {
            client.resultAuth(m[2]);
            return;
        }
        nick = m[1];
        client.resultAuth(null);
    }

    private String[] parseMessage(String s) {
        return s.split(Const.SEPARATOR, 3);
    }

    public void sendMessage(String msg) throws IOException {
        if (!connected) {
            client.showMessage("No connection");
            return;
        }
        out.writeUTF(msg);
        out.flush();
    }

    public void sendCommand(String cmd, String[] args) throws IOException {
        if (cmd.equals(Const.CMD_AUTH))
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

