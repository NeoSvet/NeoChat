package ru.neosvet.chat.base.log;

import java.io.*;
import java.util.ArrayList;

public class Logger {
    private final String path;
    private final int limit;
    private int count = 0;
    private int number;
    private boolean opened = false;
    private File log;
    private BufferedWriter writer;

    public Logger(String path, int limit) {
        this.path = path;
        this.limit = limit;
    }

    public void start() throws IOException {
        log = getFirstFile();
        open();
    }

    public void append(String owner, String line) throws IOException {
        if (!opened)
            return;
        writer.append(String.valueOf(System.currentTimeMillis()));
        writer.newLine();
        writer.append(owner);
        writer.newLine();
        writer.append(line);
        writer.newLine();
        writer.flush();
        count++;
        if (count == limit) {
            close();
            log = getNewFile();
            open();
        }
    }

    public void close() throws IOException {
        if (!opened)
            return;
        opened = false;
        writer.close();
    }

    public ArrayList<Record> getLastRecords(int count) throws IOException {
        ArrayList<Record> records = new ArrayList<>();
        Record record;
        close();
        BufferedReader br = new BufferedReader(new FileReader(log));
        int k = 0;
        int n = number;
        int i = -1;
        String s;
        File file;

        do {
            while ((s = br.readLine()) != null) {
                record = new Record();
                record.setTime(Long.parseLong(s));
                record.setOwner(br.readLine());
                record.setMsg(br.readLine());
                if (i > -1) {
                    records.add(i, record);
                    i++;
                } else {
                    records.add(record);
                }
                k++;
            }

            if (--n < 0)
                break;
            file = new File(getFileName(n));
            if (!file.exists())
                break;
            br.close();
            br = new BufferedReader(new FileReader(file));
            i = 0;

        } while (k < count);

        br.close();
        open();

        while (k > count) {
            records.remove(0);
            k--;
        }

        return records;
    }

    private void open() throws IOException {
        writer = new BufferedWriter(new FileWriter(log, true));
        opened = true;
    }

    private File getFirstFile() throws IOException {
        number = 0;
        File file;
        do {
            number++;
            file = new File(getFileName(number));
        } while (file.exists());
        number--;
        file = new File(getFileName(number));
        count = countLines(file);
        if (count >= limit)
            return getNewFile();
        return file;
    }

    private String getFileName(int n) {
        return path + String.format("/log%d.txt", n);
    }

    private File getNewFile() {
        File file;
        do {
            number++;
            file = new File(getFileName(number));
        } while (file.exists());
        count = 0;
        return file;
    }

    private int countLines(File file) throws IOException {
        if (!file.exists())
            return 0;
        BufferedReader br = new BufferedReader(new FileReader(file));
        int n = 0;
        while (br.readLine() != null) {
            n++;
        }
        return n / 3;
    }
}
