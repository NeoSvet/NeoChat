package ru.neosvet.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import ru.neosvet.chat.base.*;
import ru.neosvet.chat.base.log.LogSQL;
import ru.neosvet.chat.base.log.MyLogger;
import ru.neosvet.chat.base.log.Record;
import ru.neosvet.chat.base.requests.LogRequest;
import ru.neosvet.chat.base.requests.MessageRequest;
import ru.neosvet.chat.base.requests.PrivateMessageRequest;
import ru.neosvet.chat.server.auth.AuthSQL;
import ru.neosvet.chat.server.auth.AuthService;
import ru.neosvet.chat.server.auth.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class Server {
    public static final String NICK = "Server";
    private final String HYSTORY_PATH = "jdbc:sqlite:src/main/resources/server/chat.db";
    private final int HYSTORY_LIMIT = 100;
    private ServerSocket serverSocket;
    private AuthSQL authService;
    private int count_users = 0;
    private Map<String, ClientHandler> clients = new HashMap<>();
    private MyLogger history;
    private Logger logger;

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
        RequestParser parser = new RequestParser(NICK);
        while (true) {
            String s = scan.nextLine();
            if (s.contains(Cmd.ID)) {
                s = replaceIdToNick(s);
            }
            if (parser.parse(s)) {
                switch (parser.getResult().getType()) {
                    case STOP:
                        stop();
                        return;
                    case LIST:
                        System.out.println(getUsersListToString());
                        continue;
                    case MSG_PRIVATE:
                        sendPrivateMessage(NICK, (PrivateMessageRequest) parser.getResult());
                        continue;
                    case LOG:
                        LogRequest lr = (LogRequest) parser.getResult();
                        try {
                            showLog(lr.getCount());
                        } catch (Exception e) {
                            logger.error("Failed to get records: " + e.getMessage());
                        }
                        continue;
                }
                if (parser.HasRecipient()) {
                    if (clients.containsKey(parser.getRecipient())) {
                        clients.get(parser.getRecipient()).sendRequest(parser.getResult());
                    } else {
                        System.out.println("Command no sent: no recipient");
                    }
                    continue;
                }
                broadcastRequest(NICK, parser.getResult());
                continue;
            }
            broadcastRequest(NICK, RequestFactory.createPublicMsg(NICK, s));
        }
    }

    private String replaceIdToNick(String s) {
        String[] m = s.split(" ");
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < m.length; i++) {
                if (m[i].startsWith(Cmd.ID)) {
                    int id = Integer.parseInt(m[i].substring(Cmd.ID.length()));
                    User user = authService.getUser(id);
                    sb.append(user.getNick());
                } else
                    sb.append(m[i]);
                if (i < m.length - 1)
                    sb.append(" ");
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("replaceIdToNick: " + e.getMessage());
        }
        return s;
    }

    private void stop() throws IOException {
        broadcastRequest(NICK, RequestFactory.createStop());
        try {
            history.close();
        } catch (Exception e) {
            logger.warn("History could not stop: " + e.getMessage());
        }
        authService.close();
        serverSocket.close();
        logInfo("Server stopped.");
        System.exit(0);
    }

    public void start(int port) throws IOException {
        logger = (Logger) LogManager.getLogger();

        this.serverSocket = new ServerSocket(port);
        this.authService = new AuthSQL();
        authService.start();
        authService.addDefaultUsers();
        System.out.println("Server started");
        history = new LogSQL();
        try {
            history.start(HYSTORY_PATH, HYSTORY_LIMIT);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("History could not start: " + e.getMessage());
        }

        new Thread(() -> {
            try {
                while (true) {
                    waitNewConnection();
                }
            } catch (IOException e) {
                logger.error("Error Server: " + e.getMessage());
            }
        }).start();

        logInfo("Server started.");
        chat();
    }

    private void showLog(int count) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss]");
        String curDate = dateFormat.format(Date.from(Instant.now()));
        String newDate;
        for (Record record : history.getLastRecords(count)) {
            newDate = dateFormat.format(record.getDate());
            if (!curDate.equals(newDate)) {
                curDate = newDate;
                System.out.println("______" + newDate + "______");
            }
            System.out.print(timeFormat.format(record.getDate()));
            if (record.hasOwner()) {
                System.out.printf("<%s>%s%n", record.getOwner(), record.getMsg());
            } else {
                System.out.printf("%s%n", record.getMsg());
            }
        }
    }

    public void logInfo(String msg) {
        logger.info(msg);
    }

    public void logWarn(String msg) {
        logger.warn(msg);
    }

    public void logError(String msg) {
        logger.error(msg);
    }

    private void waitNewConnection() throws IOException {
        Socket clientSocket = serverSocket.accept();
        count_users++;
        ClientHandler clientHandler = new ClientHandler(this, clientSocket, count_users);
        clientHandler.handle();
    }

    public void broadcastRequest(String sender, Request request) throws IOException {
        if (request.getType() == RequestType.MSG_PUBLIC) {
            MessageRequest msg = (MessageRequest) request;
            try {
                history.append(msg.getOwner(), msg.getMsg());
            } catch (Exception e) {
                logger.warn("history.append: " + e.getMessage());
            }
        } else {
            logger.info(sender + ": " + request.toString());
        }
        System.out.printf("<%s>%s%n", getIdByNick(sender), request.toString());
        if (!sender.equals(NICK) && isNotClientRequest(request.getType())) {
            return;
        }
        for (ClientHandler client : clients.values()) {
            if (client.getNick().equals(sender)) {
                continue;
            }
            client.sendRequest(request);
        }
    }

    private String getIdByNick(String nick) {
        if (!clients.containsKey(nick))
            return nick;
        return "id:" + clients.get(nick).getId();
    }

    private boolean isNotClientRequest(RequestType type) {
        return type == RequestType.STOP || type == RequestType.BYE || type == RequestType.KICK;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNickBusy(String nick) {
        if (nick.equals(this.NICK))
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

    public String getUsersListToString() {
        StringBuilder sb = new StringBuilder("User list:\n");
        for (String nick : clients.keySet()) {
            sb.append("id:");
            sb.append(clients.get(nick).getId());
            sb.append(" nick:");
            sb.append(nick);
            sb.append("\n");
        }
        return sb.toString();
    }

    public String[] getUsersList() {
        String[] users = new String[clients.size() + 1];
        users[0] = NICK;
        int i = 1;
        for (String nick : clients.keySet()) {
            users[i++] = nick;
        }
        return users;
    }

    public void sendPrivateMessage(String sender, PrivateMessageRequest request) throws IOException {
        if (NICK.equals(request.getRecipient())) {
            System.out.printf("Private message from %s: %s%n", sender, request.getMsg());
            return;
        }
        if (clients.containsKey(request.getRecipient())) {
            clients.get(request.getRecipient()).sendRequest(request);
            return;
        }
        if (NICK.equals(sender)) {
            System.out.printf("User with nick '%s' is missing%n", request.getRecipient());
            return;
        }
        if (clients.containsKey(sender)) {
            clients.get(sender).sendRequest(RequestFactory.createError("Message not sent",
                    String.format("User with nick '%s' is missing", request.getRecipient())));
        }
    }

    public void changeNick(String old_nick, String new_nick) throws IOException {
        ClientHandler client = clients.get(old_nick);
        broadcastRequest(old_nick, RequestFactory.createRename(old_nick, new_nick));
        clients.remove(old_nick);
        clients.put(new_nick, client);
    }

    public ArrayList<Record> getChatHistory(int count) throws Exception {
        return history.getLastRecords(count);
    }
}
