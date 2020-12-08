package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class MessageRequest implements Request {
    private RequestType type;
    private String msg;

    public MessageRequest(RequestType type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }
}
