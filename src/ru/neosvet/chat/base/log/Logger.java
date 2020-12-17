package ru.neosvet.chat.base.log;

import java.io.IOException;
import java.util.ArrayList;

public interface Logger {

    void start(String path, int limit) throws Exception;

    void append(String owner, String line) throws Exception;

    void close() throws Exception;

    ArrayList<Record> getLastRecords(int count) throws Exception;
}
