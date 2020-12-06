package ru.neosvet.chat.base;

import java.io.DataOutputStream;
import java.io.IOException;

public class Chat {
    public static String[] parseMessage(String s) {
        return s.split(Const.SEPARATOR);
    }
    public static void sendCommand(DataOutputStream out, String cmd, String... args) throws IOException {
        StringBuilder builder = new StringBuilder(cmd);
        for (String s : args) {
            builder.append(Const.SEPARATOR);
            builder.append(s);
        }
        out.writeUTF(builder.toString());
        out.flush();
    }
}
