package ru.neosvet.chat.base;

public interface Cmd {
    String EXIT = "/exit";
    String STOP = "/stop";
    String CONNECT = "/connect";
    String AUTH = "/auth";
    String REG = "/reg";
    String ERROR = "/error";
    String JOIN = "/join";
    String LIST = "/list";
    String LEFT = "/left";
    String BYE = "/bye";
    String KICK = "/kick";
    String NICK = "/nick";
    String ID = "/id";
    String LOG = "/log";
    String PORT = "/port";
    String MSG_PRIVATE = "/w";
    String MSG_PUBLIC = "/m";
}
