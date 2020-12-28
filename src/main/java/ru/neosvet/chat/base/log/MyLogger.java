package ru.neosvet.chat.base.log;

import java.util.ArrayList;

public interface MyLogger {

    void start(String path, int limit) throws Exception;

    void append(String owner, String line) throws Exception;

    void close() throws Exception;

    ArrayList<Record> getLastRecords(int count) throws Exception;
}
