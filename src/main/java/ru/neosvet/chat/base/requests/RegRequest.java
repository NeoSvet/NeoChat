package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class RegRequest implements Request {
    private String login, password, nick;

    public RegRequest(String login, String password, String nick) {
        this.login = login;
        this.password = password;
        this.nick = nick;
    }

    @Override
    public RequestType getType() {
        return RequestType.REG;
    }

    @Override
    public String toString() {
        return String.format("[%s]%s@%s, %s", getType().toString(), login, password, nick);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNick() {
        return nick;
    }
}
