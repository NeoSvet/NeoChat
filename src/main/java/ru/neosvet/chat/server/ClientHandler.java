package ru.neosvet.chat.server;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestFactory;
import ru.neosvet.chat.base.requests.AuthRequest;
import ru.neosvet.chat.base.requests.LogRequest;
import ru.neosvet.chat.base.requests.PrivateMessageRequest;
import ru.neosvet.chat.base.requests.UserRequest;
import ru.neosvet.chat.server.auth.AuthService;
import ru.neosvet.chat.server.auth.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private final int AUTH_TIMEOUT = 120000;
    private Server srv;
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nick = null;
    private int id;
    private boolean connected = false;
    private Timer timer;

    public ClientHandler(Server srv, Socket clientSocket, int id) {
        this.srv = srv;
        this.clientSocket = clientSocket;
        this.id = id;
        srv.logInfo("User #" + id + " connected!");
    }

    public void handle() throws IOException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        connected = true;
        new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (IOException e) {
                if (!connected)
                    return;
                srv.logError("Error ClientHandler: " + e.getMessage());
                srv.unSubscribe(this);
            }
        }).start();
    }

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                connected = false;
                sendRequest(RequestFactory.createKick());
                clientSocket.close();
            } catch (IOException e) {
                srv.logWarn("ClientHandler.TimeTask: " + e.getMessage());
            }
        }
    };

    private void authentication() throws IOException {
        timer = new Timer();
        timer.schedule(task, AUTH_TIMEOUT);
        while (true) {
            Request request = readRequest();
            if (request == null)
                continue;

            //System.out.printf("Message from user #%d: %s%n", id, msg);
            switch (request.getType()) {
                case EXIT:
                    connected = false;
                    sendRequest(RequestFactory.createBye());
                    clientSocket.close();
                    return;
                case AUTH:
                    AuthRequest auth = (AuthRequest) request;
                    AuthService authService = srv.getAuthService();
                    User user = authService.getUser(auth.getLogin(), auth.getPassword());
                    if (user != null) {
                        nick = user.getNick();
                        if (srv.isNickBusy(nick)) {
                            sendRequest(RequestFactory.createError("Auth", "User is busy"));
                        } else {
                            srv.logInfo(String.format("User #%d auth as %s (id %d)", id, nick, user.getId()));
                            id = user.getId();
                            timer.cancel();
                            sendRequest(RequestFactory.createNick(nick));
                            sendRequest(RequestFactory.createList(srv.getUsersList()));
                            srv.broadcastRequest(nick, RequestFactory.createJoin(nick));
                            srv.subscribe(this);
                            return;
                        }
                    } else {
                        sendRequest(RequestFactory.createError("Auth", "Login or password is incorrect"));
                    }
                    break;
                default:
                    sendRequest(RequestFactory.createError("Auth", "You are not authorized"));
                    break;
            }
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            Request request = readRequest();
            if (request == null)
                continue;

            switch (request.getType()) {
                case EXIT:
                    leaveChat();
                    return;
                case MSG_PRIVATE:
                    srv.sendPrivateMessage(nick,
                            (PrivateMessageRequest) request);
                    break;
                case MSG_PUBLIC:
                    srv.broadcastRequest(nick, request);
                    break;
                case NICK:
                    UserRequest user = (UserRequest) request;
                    if (srv.getAuthService().changeNick(id, user.getNick())) {
                        srv.changeNick(nick, user.getNick());
                        nick = user.getNick();
                        ;
                        sendRequest(user);
                    } else {
                        sendRequest(RequestFactory.createError("Error", "Nick is busy"));
                    }
                    break;
                case LIST:
                    sendRequest(RequestFactory.createPublicMsg(
                            Server.NICK, srv.getUsersListToString()));
                    break;
                case LOG:
                    LogRequest lr = (LogRequest) request;
                    try {
                        sendRequest(RequestFactory.createRecords(srv.getChatHistory(lr.getCount())));
                    } catch (Exception e) {
                        srv.logError("ClientHandler.LogRequest: " + e.getMessage());
                        sendRequest(RequestFactory.createError("Error", "Failed to get records"));
                    }
                    break;
            }
        }
    }

    private Request readRequest() throws IOException {
        try {
            return (Request) in.readObject();
        } catch (ClassNotFoundException e) {
            srv.logWarn("ClientHandler.readRequest: Unknown request. " + e.getMessage());
            return null;
        }
    }

    private void leaveChat() throws IOException {
        connected = false;
        sendRequest(RequestFactory.createBye());
        srv.broadcastRequest(nick, RequestFactory.createLeft(nick));
        srv.unSubscribe(this);
        clientSocket.close();
    }

    public String getNick() {
        return nick;
    }

    public void sendRequest(Request request) throws IOException {
        //System.out.println("send to " + nick + ": " + request.toString());
        out.writeObject(request);
        out.flush();
    }

    public int getId() {
        return id;
    }
}
