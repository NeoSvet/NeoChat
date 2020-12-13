package ru.neosvet.chat.server.auth;

public interface AuthService {
    boolean start();

    User getUser(String login, String password);

    void close();
}
