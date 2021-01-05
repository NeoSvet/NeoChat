package ru.neosvet.chat.server.auth;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestFactory;
import ru.neosvet.chat.base.RequestType;
import ru.neosvet.chat.base.requests.NumberRequest;

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
            if (client.getLogin().equals(login) && client.getPassword().equals(password)) {
                return client;
            }
        }
        return null;
    }

    @Override
    public User getUser(int id) {
        for (User client : clients) {
            if (client.getId() == id) {
                return client;
            }
        }
        return null;
    }

    @Override
    public boolean changeNick(int id, String new_nick) {
        User user = null;
        for (User client : clients) {
            if (client.getNick().equals(new_nick))
                return false;
            if (client.getId() == id)
                user = client;
        }
        if (user == null)
            return false;
        user.setNick(new_nick);
        return true;
    }

    @Override
    public Request regUser(String login, String password, String nick) {
        for (User client : clients) {
            if (client.getLogin().equals(login))
                return RequestFactory.createError("Reg", "Login is busy");
            if (client.getNick().equals(nick))
                return RequestFactory.createError("Reg", "Nick is busy");
        }
        int id = clients.size() + 1;
        clients.add(new User(id, login, password, nick));
        return new NumberRequest(RequestType.REG, id);
    }

    @Override
    public boolean delUser(int id) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId() == id) {
                clients.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        System.out.println("AuthService stopped");
    }
}
