package ru.neosvet.chat.server;

import ru.neosvet.chat.Const;
import ru.neosvet.chat.server.auth.AuthSample;
import ru.neosvet.chat.server.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    public final String nick = "Server";
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
            String s = scan.next();
            if (s.equals(Const.CMD_STOP)) {
                stop();
                return;
            }
            broadcastMessage(s, nick, false);
        }
    }

    private void stop() throws IOException {
        broadcastMessage("Server stopped", nick, true);
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

    public void broadcastMessage(String msg, String sender, boolean isInfoMsg) throws IOException {
        if (!sender.equals(nick))
            System.out.printf("Received message from %s: %s%n", sender, msg);
        for (ClientHandler client : clients.values()) {
            if (client.getNick().equals(sender)) {
                continue;
            }
            client.sendMessage(isInfoMsg ? null : sender, msg);
        }
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
}
