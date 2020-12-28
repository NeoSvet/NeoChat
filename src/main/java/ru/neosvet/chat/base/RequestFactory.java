package ru.neosvet.chat.base;

import ru.neosvet.chat.base.log.Record;
import ru.neosvet.chat.base.requests.*;

import java.util.ArrayList;

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

    public static Request createPublicMsg(String sender, String msg) {
        return new MessageRequest(RequestType.MSG_PUBLIC, sender, msg);
    }

    public static Request createRename(String old_nick, String new_nick) {
        return new MessageRequest(RequestType.RENAME, old_nick, new_nick);
    }

    public static Request createPrivateMsg(String sender, String recipient, String msg) {
        return new PrivateMessageRequest(sender, recipient, msg);
    }

    public static Request createError(String title, String msg) {
        return new MessageRequest(RequestType.ERROR, title, msg);
    }

    public static Request createNick(String nick) {
        return new UserRequest(RequestType.NICK, nick);
    }

    public static Request createJoin(String nick) {
        return new UserRequest(RequestType.JOIN, nick);
    }

    public static Request createLeft(String nick) {
        return new UserRequest(RequestType.LEFT, nick);
    }

    public static Request createList(String[] users) {
        return new ListRequest(users);
    }

    public static Request createLog(int count) {
        return new LogRequest(count);
    }

    public static Request createRecords(ArrayList<Record> records) {
        return new RecordsRequest(records);
    }

}
