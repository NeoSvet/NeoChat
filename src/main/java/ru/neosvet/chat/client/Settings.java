package ru.neosvet.chat.client;

import ru.neosvet.chat.base.Const;

import java.io.*;

public class Settings {
    private final String PATH = "/src/main/resources/client/settings.ini";
    private final String HOST = "host=", PORT = "port=", LOGIN = "login=", PASSWORD = "password=";
    private String host = null, port = null, login = null, password = null;
    private File file;

    public Settings() {
        file = new File(System.getProperty("user.dir") + PATH);
        if (!file.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine()) != null) {
                if (s.contains(HOST)) {
                    host = s.substring(HOST.length());
                } else if (s.contains(PORT)) {
                    port = s.substring(PORT.length());
                } else if (s.contains(LOGIN)) {
                    login = s.substring(LOGIN.length());
                } else if (s.contains(PASSWORD)) {
                    password = s.substring(PASSWORD.length());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            if (host != null) {
                bw.write(HOST);
                bw.write(host);
                bw.newLine();
            }
            if (port != null) {
                bw.write(PORT);
                bw.write(port);
                bw.newLine();
            }
            if (login != null) {
                bw.write(LOGIN);
                bw.write(login);
                bw.newLine();
            }
            if (password != null) {
                bw.write(PASSWORD);
                bw.write(password);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        if (host == null)
            return Const.DEFAULT_HOST;
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLogin() {
        if (login == null)
            return "";
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        if (password == null)
            return "";
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        if (port == null)
            return String.valueOf(Const.DEFAULT_PORT);
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
