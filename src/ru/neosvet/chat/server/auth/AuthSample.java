package ru.neosvet.chat.server.auth;

import java.util.ArrayList;
import java.util.List;

public class AuthSample implements AuthService {
    private static final List<User> clients = new ArrayList<>();

    @Override
    public boolean start() {
        clients.add(new User(1, "user1", "1111", "Борис_Николаевич"));
        clients.add(new User(2, "user2", "2222", "Мартин_Некотов"));
        clients.add(new User(3, "user3", "3333", "Гендальф_Серый"));
        System.out.println("AuthService started");
        return true;
    }

    @Override
    public User getUser(String login, String password) {
        for (User client : clients) {
            if(client.getLogin().equals(login) && client.getPassword().equals(password)) {
                return client;
            }
        }
        return null;
    }

    @Override
    public void close() {
        System.out.println("AuthService stopped");
    }
}
