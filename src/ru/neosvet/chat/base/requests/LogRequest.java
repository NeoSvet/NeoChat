package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class LogRequest implements Request {
    private int count;

    public LogRequest(int count) {
        this.count = count;
    }

    @Override
    public RequestType getType() {
        return RequestType.LOG;
    }

    @Override
    public String toString() {
        return String.format("[%s]count = %d", getType().toString(), count);
    }

    public int getCount() {
        return count;
    }
}
