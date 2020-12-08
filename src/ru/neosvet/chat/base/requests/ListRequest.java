package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

import java.util.List;

public class ListRequest implements Request {
    private final List<String> users;

    public ListRequest(List<String> users) {
        this.users = users;
    }

    @Override
    public RequestType getType() {
        return RequestType.LIST;
    }

    public List<String> getUsers() {
        return users;
    }
}
