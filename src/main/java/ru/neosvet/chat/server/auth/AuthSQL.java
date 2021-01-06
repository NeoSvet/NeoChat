package ru.neosvet.chat.server.auth;

import ru.neosvet.chat.base.Request;
import ru.neosvet.chat.base.RequestFactory;
import ru.neosvet.chat.base.RequestType;
import ru.neosvet.chat.base.requests.NumberRequest;

import java.sql.*;

public class AuthSQL implements AuthService {
    private final String DB_PATH = "jdbc:sqlite:src/main/resources/server/users.db";
    private final int INDEX_LOGIN = 1, INDEX_PASSWORD = 2, INDEX_NICK = 3;
    private PreparedStatement insertUser;
    private Connection connection;
    private Statement stmt;

    @Override
    public boolean start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_PATH);
            stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users ("
                    + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "	login VARCHAR(32) NOT NULL UNIQUE,"
                    + "	password VARCHAR(32) NOT NULL,"
                    + "	nick VARCHAR(32) UNIQUE);";
            stmt.execute(sql);
            System.out.println("AuthService started");
            insertUser = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUES (?, ?, ?)");
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User getUser(String login, String password) {
        User client = null;
        try {
            ResultSet rs = stmt.executeQuery(String.format("SELECT id, password, nick FROM users WHERE login = '%s'", login));
            if (rs.isClosed()) //login is incorrect
                return null;
            String passDB = rs.getString("password");
            if (passDB != null && passDB.equals(password)) {
                int id = rs.getInt("id");
                String nick = rs.getString("nick");
                client = new User(id, login, password, nick);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }

    @Override
    public User getUser(int id) {
        User client = null;
        try {
            ResultSet rs = stmt.executeQuery(String.format("SELECT login, password, nick FROM users WHERE id = %d", id));
            if (rs.isClosed()) //id is incorrect
                return null;

            String login = rs.getString("login");
            String password = rs.getString("password");
            String nick = rs.getString("nick");
            client = new User(id, login, password, nick);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }

    @Override
    public boolean changeNick(int id, String new_nick) {
        try {
            ResultSet rs = stmt.executeQuery(String.format("SELECT login FROM users WHERE nick = '%s'", new_nick));
            if (!rs.isClosed()) { //nick is busy
                rs.close();
                return false;
            }

            int result = stmt.executeUpdate(String.format("UPDATE users SET nick = '%s' WHERE id = %d", new_nick, id));
            return result == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Request regUser(String login, String password, String nick) {
        try {
            ResultSet rs = stmt.executeQuery(String.format("SELECT login FROM users WHERE login = '%s'", login));
            if (!rs.isClosed()) { //login is busy
                rs.close();
                return RequestFactory.createError("Reg", "Login is busy");
            }
            rs = stmt.executeQuery(String.format("SELECT login FROM users WHERE nick = '%s'", nick));
            if (!rs.isClosed()) { //nick is busy
                rs.close();
                return RequestFactory.createError("Reg", "Nick is busy");
            }

            insertUser.setString(INDEX_LOGIN, login);
            insertUser.setString(INDEX_PASSWORD, password);
            insertUser.setString(INDEX_NICK, nick);
            int result = insertUser.executeUpdate();
            if (result == 1) {
                rs = stmt.executeQuery(String.format("SELECT id FROM users WHERE login = '%s'", login));
                return new NumberRequest(RequestType.REG, rs.getInt("id"));
            }
            return RequestFactory.createError("Reg", "User was not created");
        } catch (SQLException e) {
            e.printStackTrace();
            return RequestFactory.createError("Reg", "Server error: " + e.getMessage());
        }
    }

    @Override
    public boolean delUser(int id) {
        try {
            int result = stmt.executeUpdate(String.format("DELETE FROM users WHERE id = %d", id));
            return result == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void close() {
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDefaultUsers() {
        try {
            User[] users = new User[]{
                    new User(1, "user1", "1111", "Борис_Николаевич"),
                    new User(2, "user2", "2222", "Мартин_Некотов"),
                    new User(3, "user3", "3333", "Гендальф_Серый")
            };

            for (User user : users) {
                insertUser.setString(INDEX_LOGIN, user.getLogin());
                insertUser.setString(INDEX_PASSWORD, user.getPassword());
                insertUser.setString(INDEX_NICK, user.getNick());

                insertUser.addBatch();
            }

            insertUser.executeBatch();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }
}
