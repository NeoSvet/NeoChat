package ru.neosvet.chat.server;

import ru.neosvet.chat.base.*;
import ru.neosvet.chat.base.requests.MessageRequest;
import ru.neosvet.chat.base.requests.PrivateMessageRequest;
import ru.neosvet.chat.server.auth.AuthSample;
import ru.neosvet.chat.server.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private final String nick = "Server";
    private ServerSocket serverSocket;
    private AuthService authService;
    private int count_users = 0;
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
            } else if (s.startsWith(Cmd.MSG_PRIVATE)) {
                String[] m = s.split(" ", 3);
                sendPrivateMessage(nick, (PrivateMessageRequest)
                        RequestFactory.createPrivateMsg(nick, m[1], m[2]));
                continue;
            }
            broadcastRequest(nick, RequestFactory.createGlobalMsg(nick, s));
        }
    }

    private void stop() throws IOException {
        broadcastRequest(nick, RequestFactory.createStop());
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
        count_users++;
        ClientHandler clientHandler = new ClientHandler(this, clientSocket, count_users);
        clientHandler.handle();
    }

    public void broadcastRequest(String sender, Request request) throws IOException {
        System.out.printf("<%s>%s%n", sender, request.toString());
        if (!sender.equals(nick) && isNotClientRequest(request.getType())) {
            return;
        }
        for (ClientHandler client : clients.values()) {
            if (client.getNick().equals(sender)) {
                continue;
            }
            client.sendRequest(request);
        }
    }

    private boolean isNotClientRequest(RequestType type) {
        return type == RequestType.STOP || type == RequestType.BYE || type == RequestType.KICK;
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
        String[] users = new String[clients.size() + 1];
        users[0] = nick;
        int i = 1;
        for (String nick : clients.keySet()) {
            users[i++] = nick;
        }
        return users;
    }

    public void sendPrivateMessage(String sender, PrivateMessageRequest request) throws IOException {
        if (nick.equals(request.getRecipient())) {
            System.out.printf("Private message from %s: %s%n", sender, request.getMsg());
            return;
        }
        if (clients.containsKey(request.getRecipient())) {
            clients.get(request.getRecipient()).sendRequest(request);
            return;
        }
        if (nick.equals(sender)) {
            System.out.printf("User with nick '%s' is missing%n", request.getRecipient());
            return;
        }
        if (clients.containsKey(sender)) {
            clients.get(sender).sendRequest(RequestFactory.createError("Message not sent",
                    String.format("User with nick '%s' is missing", request.getRecipient())));
        }
    }
}
