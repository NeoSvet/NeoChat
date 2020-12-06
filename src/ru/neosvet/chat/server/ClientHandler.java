package ru.neosvet.chat.server;

import ru.neosvet.chat.base.Chat;
import ru.neosvet.chat.base.Cmd;
import ru.neosvet.chat.server.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private final int AUTH_TIMEOUT = 120000;
    private Server srv;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick = null;
    private int number;
    private boolean connected = false;

    public ClientHandler(Server srv, Socket clientSocket, int number) {
        this.srv = srv;
        this.clientSocket = clientSocket;
        this.number = number;
        System.out.printf("User #%d connected!%n", number);
    }

    public void handle() {
        new Thread(() -> {
            try {
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());
                connected = true;
                authentication();
                readMessage();
            } catch (IOException e) {
                if (!connected)
                    return;
                e.printStackTrace();
                System.out.println("Error ClientHandler: " + e.getMessage());
                srv.unSubscribe(this);
            }
        }).start();
    }

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (isAuthUser())
                return;
            try {
                connected = false;
                sendCommand(Cmd.KICK, "");
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private boolean isAuthUser() {
        return number == 0;
    }

    private void authentication() throws IOException {
        Timer t = new Timer();
        t.schedule(task, AUTH_TIMEOUT);
        while (true) {
            String msg = in.readUTF();
            System.out.printf("Message from user #%d: %s%n", number, msg);
            if (msg.startsWith(Cmd.EXIT)) {
                connected = false;
                sendCommand(Cmd.BYE, "");
                clientSocket.close();
                return;
            }
            if (msg.startsWith(Cmd.AUTH)) {
                String[] m = Chat.parseMessage(msg);
                String login = m[1];
                String password = m[2];

                AuthService authService = srv.getAuthService();
                nick = authService.getNickByLoginAndPassword(login, password);
                if (nick != null) {
                    if (srv.isNickBusy(nick)) {
                        sendCommand(Cmd.AUTH, Cmd.ERROR, "Nick is busy");
                    } else {
                        System.out.printf("User #%d auth as %s%n", number, nick);
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

    private void readMessage() throws IOException {
        while (true) {
            String[] m = Chat.parseMessage(in.readUTF());
            System.out.printf("Message from %s: %s%n", nick, Arrays.toString(m));
            switch (m[0]) {
                case Cmd.EXIT:
                    leaveChat();
                    return;
                case Cmd.MSG_PRIVATE:
                    srv.sendPrivateMessage(nick, m[1], m[2]);
                    break;
                case Cmd.MSG_CLIENT:
                    srv.broadcastMessage(nick, m[1]);
                    break;
            }
        }
    }

    private void leaveChat() throws IOException {
        connected = false;
        sendCommand(Cmd.BYE, "");
        srv.broadcastCommand(nick, Cmd.LEFT, nick);
        srv.unSubscribe(this);
        clientSocket.close();
    }

    public String getNick() {
        return nick;
    }

    public void sendCommand(String cmd, String... args) throws IOException {
        Chat.sendCommand(out, cmd, args);
    }
}
