package ru.neosvet.chat.base.requests;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestType;
import ru.neosvet.chat.base.log.Record;

import java.util.ArrayList;
import java.util.Arrays;

public class RecordsRequest implements Request {
    private final ArrayList<Record> records;

    public RecordsRequest(ArrayList<Record> records) {
        this.records = records;
    }

    @Override
    public RequestType getType() {
        return RequestType.RECORDS;
    }

    @Override
    public String toString() {
        return String.format("[%s]%s", getType().toString(), Arrays.toString(records.toArray()));
    }

    public ArrayList<Record> getRecords() {
        return records;
    }
}
