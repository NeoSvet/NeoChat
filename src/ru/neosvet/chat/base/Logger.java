package ru.neosvet.chat.base;

import java.io.*;

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

    private void open() throws IOException {
        writer = new BufferedWriter(new FileWriter(log, true));
        opened = true;
    }

    private File getFirstFile() throws IOException {
        number = 0;
        File file;
        do {
            number++;
            file = new File(getFileName());
        } while (file.exists());
        number--;
        file = new File(getFileName());
        count = countLines(file);
        if (count >= limit)
            return getNewFile();
        return file;
    }

    private String getFileName() {
        return path + String.format("/log%d.txt", number);
    }

    private File getNewFile() {
        File file;
        do {
            number++;
            file = new File(getFileName());
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
