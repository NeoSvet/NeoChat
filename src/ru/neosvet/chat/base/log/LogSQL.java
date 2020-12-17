package ru.neosvet.chat.base.log;

import java.sql.*;
import java.util.ArrayList;

public class LogSQL implements Logger {
    private final int INDEX_TIME = 1, INDEX_OWNER = 2, INDEX_MSG = 3;
    private final String TIME = "time", OWNER = "owner", MSG = "msg";
    private Connection connection;
    private Statement stmt;
    private int limit;
    private boolean started = false;
    private PreparedStatement newRecord, getRecords;

    @Override
    public void start(String path, int limit) throws Exception {
        this.limit = limit;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(path);
        stmt = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS records ("
                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "	time BIGINT,"
                + "	owner VARCHAR(32),"
                + "	msg TEXT);";
        stmt.execute(sql);
        newRecord = connection.prepareStatement("INSERT INTO records (time, owner, msg) VALUES (?, ?, ?);");
        getRecords = connection.prepareStatement("SELECT time,owner,msg FROM records ORDER BY id LIMIT ? OFFSET ((SELECT count(*) FROM records)-?);");
        started = true;
    }

    @Override
    public void append(String owner, String line) throws Exception {
        if (!started)
            return;
        newRecord.setString(INDEX_TIME, String.valueOf(System.currentTimeMillis()));
        newRecord.setString(INDEX_OWNER, owner);
        newRecord.setString(INDEX_MSG, line);
        newRecord.execute();
    }

    @Override
    public void close() throws Exception {
        if (!started)
            return;
        connection.close();
    }

    @Override
    public ArrayList<Record> getLastRecords(int count) throws Exception {
        ArrayList<Record> records = new ArrayList<>();
        getRecords.setString(1, String.valueOf(count));
        getRecords.setString(2, String.valueOf(count));
        ResultSet rs = getRecords.executeQuery();
        if (!rs.next()) { //log is empty
            return records;
        }

        Record record;
        do {
            record = new Record();
            record.setTime(rs.getLong(TIME));
            record.setOwner(rs.getString(OWNER));
            record.setMsg(rs.getString(MSG));
            records.add(record);
        } while(rs.next());

        return records;
    }
}
