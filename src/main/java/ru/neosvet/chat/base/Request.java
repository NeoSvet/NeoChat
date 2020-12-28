package ru.neosvet.chat.base;

import java.io.Serializable;

public interface Request extends Serializable {
    RequestType getType();
}
