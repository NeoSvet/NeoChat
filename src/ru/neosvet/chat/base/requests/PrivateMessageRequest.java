package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class PrivateMessageRequest implements Request {
    private String recipient, msg;

    public PrivateMessageRequest(String recipient, String msg) {
        this.recipient = recipient;
        this.msg = msg;
    }

    @Override
    public RequestType getType() {
        return RequestType.MSG_PRIVATE;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMsg() {
        return msg;
    }
}
