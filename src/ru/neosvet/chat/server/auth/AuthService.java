package ru.neosvet.chat.server.auth;

public interface AuthService {
    boolean start();

    String getNickByLoginAndPassword(String login, String password);

    void close();
}
