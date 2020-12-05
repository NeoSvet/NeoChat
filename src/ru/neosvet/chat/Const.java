package ru.neosvet.chat;

public interface Const {
    int DEFAULT_PORT = 8189;
    String DEFAULT_HOST = "localhost";
    String SEPARATOR = Character.valueOf((char) Character.PARAGRAPH_SEPARATOR).toString();
    String CMD_EXIT = "/exit";
    String CMD_STOP = "/stop";
    String CMD_CONNECT = "/connect";
    String CMD_NICK = "/nick";
    String CMD_AUTH = "/auth";
    String CMD_ERROR = "/error";
    String MSG_PRIVATE = "/w";
    String MSG_CLIENT = "/msg";
}
