package ru.neosvet.chat.server.auth;

public interface AuthService {
    boolean start();

    void close();

    User getUser(String login, String password);

    User getUser(int id);

    boolean changeNick(int id, String new_nick);
}
