package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

import java.util.Arrays;

public class ListRequest implements Request {
    private final String[] users;

    public ListRequest(String[] users) {
        this.users = users;
    }

    @Override
    public RequestType getType() {
        return RequestType.LIST;
    }

    @Override
    public String toString() {
        return String.format("[%s]%s", getType().toString(), Arrays.toString(users));
    }

    public String[] getUsers() {
        return users;
    }
}
