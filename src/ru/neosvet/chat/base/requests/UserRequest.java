package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class UserRequest implements Request {
    private RequestType type;
    private String nick;

    public UserRequest(RequestType type, String nick) {
        this.type = type;
        this.nick = nick;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    public String getNick() {
        return nick;
    }
}
