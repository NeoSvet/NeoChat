package ru.neosvet.chat.base;

import ru.neosvet.chat.base.requests.*;

import java.util.List;

public class RequestFactory {
    public static Request createAuth(String login, String password) {
        return new AuthRequest(login, password);
    }

    public static Request createExit() {
        return new SampleRequest(RequestType.EXIT);
    }

    public static Request createStop() {
        return new SampleRequest(RequestType.STOP);
    }

    public static Request createBye() {
        return new SampleRequest(RequestType.BYE);
    }

    public static Request createKick() {
        return new SampleRequest(RequestType.KICK);
    }

    public static Request createGlobalMsg(String msg) {
        return new MessageRequest(RequestType.MSG_GLOBAL, msg);
    }

    public static Request createPrivateMsg(String recipient, String msg) {
        return new PrivateMessageRequest(recipient, msg);
    }

    public static Request createError(String msg) {
        return new MessageRequest(RequestType.ERROR, msg);
    }

    public static Request createJoin(String nick) {
        return new UserRequest(RequestType.JOIN, nick);
    }

    public static Request createLeft(String nick) {
        return new UserRequest(RequestType.LEFT, nick);
    }

    public static Request createList(List<String> users) {
        return new ListRequest(users);
    }
}
