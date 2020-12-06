package ru.neosvet.chat.server;

import ru.neosvet.chat.base.Cmd;
import ru.neosvet.chat.base.Const;
import ru.neosvet.chat.server.auth.AuthSample;
import ru.neosvet.chat.server.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private final String nick = "Server";
    private ServerSocket serverSocket;
    private AuthService authService;
    private Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start(Const.DEFAULT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error Server: " + e.getMessage());
        }
    }

    private void chat() throws IOException {
        Scanner scan = new Scanner(System.in);
        while (true) {
            String s = scan.nextLine();
            if (s.equals(Cmd.STOP)) {
                stop();
                return;
            } else if(s.startsWith(Cmd.MSG_PRIVATE)) {
                String[] m = s.split(" ", 3);
                sendPrivateMessage(nick, m[1], m[2]);
                continue;
            }
            broadcastMessage(nick, s);
        }
    }

    private void stop() throws IOException {
        broadcastCommand(nick, Cmd.STOP);
        authService.close();
        serverSocket.close();
        System.exit(0);
    }

    public void start(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authService = new AuthSample();
        authService.start();
        System.out.println("Server started");

        new Thread(() -> {
            try {
                while (true) {
                    waitNewConnection();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error Server: " + e.getMessage());
            }
        }).start();

        chat();
    }

    private void waitNewConnection() throws IOException {
        System.out.println("Waiting for connection...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("User connected!");
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public void broadcastMessage(String sender, String msg) throws IOException {
        broadcastCommand(sender, Cmd.MSG_CLIENT, sender, msg);
    }

    public void broadcastCommand(String sender, String cmd, String... args) throws IOException {
        if (!sender.equals(nick)) {
            System.out.printf("Message from %s: %s%n", sender, Arrays.toString(args));
            if (isNotClientCmd(cmd))
                return;
        }
        for (ClientHandler client : clients.values()) {
            if (client.getNick().equals(sender)) {
                continue;
            }
            client.sendCommand(cmd, args);
        }
    }

    private boolean isNotClientCmd(String cmd) {
        return cmd.equals(Cmd.STOP) || cmd.equals(Cmd.BYE);
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNickBusy(String nick) {
        if (nick.equals(this.nick))
            return true;
        if (clients.containsKey(nick))
            return true;
        return false;
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.put(clientHandler.getNick(), clientHandler);
    }

    public void unSubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler.getNick());
    }

    public String[] getUsersList() {
        String[] m = new String[clients.size() + 1];
        m[0] = nick;
        int i = 1;
        for (String nick : clients.keySet()) {
            m[i++] = nick;
        }
        return m;
    }

    public void sendPrivateMessage(String sender, String recipient, String msg) throws IOException {
        if(nick.equals(recipient)) {
            System.out.printf("Private message from %s: %s%n", sender, msg);
            return;
        }
        if (clients.containsKey(recipient)) {
            clients.get(recipient).sendCommand(Cmd.MSG_PRIVATE, sender, msg);
            return;
        }
        if(nick.equals(sender)) {
            System.out.printf("User with nick '%s' is missing%n", recipient);
            return;
        }
        if (clients.containsKey(sender)) {
            clients.get(sender).sendCommand(Cmd.ERROR, "Message not sent",
                    String.format("User with nick '%s' is missing", recipient));
        }
    }
}
