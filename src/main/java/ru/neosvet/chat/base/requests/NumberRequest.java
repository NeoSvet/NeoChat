package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class NumberRequest implements Request {
    private RequestType type;
    private int number;

    public NumberRequest(RequestType type, int number) {
        this.type = type;
        this.number = number;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("[%s]number = %d", getType().toString(), number);
    }

    public int getNumber() {
        return number;
    }
}
