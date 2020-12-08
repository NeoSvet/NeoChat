package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class PrivateMessageRequest implements Request {
    private String sender, recipient, msg;

    public PrivateMessageRequest(String sender, String recipient, String msg) {
        this.sender = sender;
        this.recipient = recipient;
        this.msg = msg;
    }

    @Override
    public RequestType getType() {
        return RequestType.MSG_PRIVATE;
    }

    @Override
    public String toString() {
        return String.format("[%s]<FROM %s TO %s>%s", getType().toString(), sender, recipient, msg);
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMsg() {
        return msg;
    }
}
