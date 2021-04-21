package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class MessageRequest implements Request {
    private RequestType type;
    private String owner, msg;

    public MessageRequest(RequestType type, String owner, String msg) {
        this.type = type;
        this.owner = owner;
        this.msg = msg;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("[%s]<%s>%s", getType().toString(), owner, msg);
    }

    public String getOwner() {
        return owner;
    }

    public String getMsg() {
        return msg;
    }
}
