package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;

public class SampleRequest implements Request {
    private RequestType type;

    public SampleRequest(RequestType type) {
        this.type = type;
    }

    @Override
    public RequestType getType() {
        return type;
    }
}
