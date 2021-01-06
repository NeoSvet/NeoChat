package ru.neosvet.chat.server.auth;

import java.util.Objects;

public class User {

    private String login;
    private String password;
    private String nick;
    private int id;

    public User(int id, String login, String password, String nick) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.nick = nick;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNick() {
        return nick;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) && Objects.equals(password, user.password) && Objects.equals(nick, user.nick);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, nick);
    }
}

