package ru.neosvet.chat.server.auth;

public interface AuthService {
    void start();

    String getNickByLoginAndPassword(String login, String password);

    void close();
}
