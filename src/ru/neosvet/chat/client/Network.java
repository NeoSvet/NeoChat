package ru.neosvet.chat.client;

import ru.neosvet.chat.Const;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private ChatController viewer;
    private boolean connected = false;

    public Network(ChatController viewer) {
        this.viewer = viewer;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        connected = true;
    }

    public void close(String nick) {
        if (!connected)
            return;
        try {
            out.writeUTF("<" + nick + ">/exit");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void waitMessage() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (message.equals(Const.CMD_STOP)) {
                        connected = false;
                        viewer.showMessage("Connection interrupted");
                        return;
                    }
                    viewer.showMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                viewer.showErrorMessage(e.getMessage());
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendMessage(String msg) throws Exception {
        if (!connected) {
            try {
                viewer.showMessage("Message not sent: no connection");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        out.writeUTF(msg);
    }
}

