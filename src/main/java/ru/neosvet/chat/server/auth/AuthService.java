package ru.neosvet.chat.server.auth;

import ru.neosvet.chat.base.Request;

public interface AuthService {
    boolean start();

    void close();

    User getUser(String login, String password);

    User getUser(int id);

    boolean changeNick(int id, String new_nick);

    Request regUser(String login, String password, String nick);

    boolean delUser(int id);
}
