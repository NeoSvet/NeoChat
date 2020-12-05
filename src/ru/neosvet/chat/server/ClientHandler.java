package ru.neosvet.chat.server;

import ru.neosvet.chat.Const;
import ru.neosvet.chat.server.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server srv;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    public ClientHandler(Server srv, Socket clientSocket) {
        this.srv = srv;
        this.clientSocket = clientSocket;
    }

    public void handle() {
        new Thread(() -> {
            try {
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());

                authentication();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error ClientHandler: " + e.getMessage());
                srv.unSubscribe(this);
            }
        }).start();
    }

    private void authentication() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith(Const.CMD_AUTH)) {
                String[] parts = parseCommand(msg);
                String login = parts[1];
                String password = parts[2];

                AuthService authService = srv.getAuthService();
                nick = authService.getNickByLoginAndPassword(login, password);
                if (nick != null) {
                    if (srv.isNickBusy(nick)) {
                        sendCommand(Const.CMD_AUTH, Const.CMD_ERROR, "Nick is busy");
                        return;
                    }
                    sendCommand(Const.CMD_AUTH, nick);
                    srv.broadcastMessage(String.format("%s joined the chat", nick), nick, true);
                    srv.subscribe(this);
                    return;
                } else {
                    sendCommand(Const.CMD_AUTH, Const.CMD_ERROR, "Login or password is incorrect");
                }

            } else {
                sendCommand(Const.CMD_ERROR, "You are not authorized");
            }
        }
    }

    private String[] parseCommand(String s) {
        return s.split(Const.SEPARATOR, 3);
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(Const.CMD_EXIT)) {
                leaveChat();
                return;
            } else if (message.startsWith(Const.MSG_PRIVATE)) {
                // String[] parts = parseCommand(message);
                System.out.println("private message | " + nick + ": " + message);
                //TODO
            } else {
                srv.broadcastMessage(message, nick, false);
            }
        }
    }

    private void leaveChat() throws IOException {
        srv.broadcastMessage(String.format("%s left the chat", nick), nick, true);
        srv.unSubscribe(this);
        clientSocket.close();
    }

    public String getNick() {
        return nick;
    }

    public void sendMessage(String sender, String msg) throws IOException {
        if (sender == null) {
            sendCommand(Const.MSG_SYSTEM, msg);
        } else {
            sendCommand(Const.MSG_CLIENT, sender, msg);
        }
    }

    public void sendCommand(String cmd, String... args) throws IOException {
        StringBuilder builder = new StringBuilder(cmd);
        for (String s : args) {
            builder.append(Const.SEPARATOR);
            builder.append(s);
        }
        out.writeUTF(builder.toString());
        out.flush();
    }
}
