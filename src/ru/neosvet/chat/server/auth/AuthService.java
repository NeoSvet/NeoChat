package ru.neosvet.chat.server.auth;

public interface AuthService {
    boolean start();

    void close();

    User getUser(String login, String password);

    boolean changeNick(int id, String new_nick);
}
