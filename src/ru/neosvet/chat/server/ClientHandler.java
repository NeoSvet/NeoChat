package ru.neosvet.chat.server;

import ru.neosvet.chat.base.Cmd;
import ru.neosvet.chat.base.Const;
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
            if (msg.startsWith(Cmd.AUTH)) {
                String[] m = parseCommand(msg);
                String login = m[1];
                String password = m[2];

                AuthService authService = srv.getAuthService();
                nick = authService.getNickByLoginAndPassword(login, password);
                if (nick != null) {
                    if (srv.isNickBusy(nick)) {
                        sendCommand(Cmd.AUTH, Cmd.ERROR, "Nick is busy");
                    } else {
                        sendCommand(Cmd.AUTH, nick);
                        sendCommand(Cmd.LIST, srv.getUsersList());
                        srv.broadcastCommand(nick, Cmd.JOIN, nick);
                        srv.subscribe(this);
                        return;
                    }
                } else {
                    sendCommand(Cmd.AUTH, Cmd.ERROR, "Login or password is incorrect");
                }

            } else {
                sendCommand(Cmd.ERROR, "Authentication", "You are not authorized");
            }
        }
    }

    private String[] parseCommand(String s) {
        return s.split(Const.SEPARATOR);
    }

    private void readMessage() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith(Cmd.EXIT)) {
                leaveChat();
                return;
            } else if (msg.startsWith(Cmd.MSG_PRIVATE)) {
                String[] m = parseCommand(msg);
                srv.sendPrivateMessage(nick, m[1], m[2]);
            } else {
                srv.broadcastMessage(nick, msg);
            }
        }
    }

    private void leaveChat() throws IOException {
        sendCommand(Cmd.BYE, "");
        srv.broadcastCommand(nick, Cmd.LEFT, nick);
        srv.unSubscribe(this);
        clientSocket.close();
    }

    public String getNick() {
        return nick;
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
