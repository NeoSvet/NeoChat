package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class AuthRequest implements Request {
    private String login, password;

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public RequestType getType() {
        return RequestType.AUTH;
    }

    @Override
    public String toString() {
        return String.format("[%s]%s@%s", getType().toString(), login, password);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
