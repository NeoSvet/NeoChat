package ru.neosvet.chat.server.auth;

import java.sql.*;

public class AuthSQL implements AuthService {
    private final String DB_PATH = "jdbc:sqlite:src/main/resources/server/users.db";
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

            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUES (?, ?, ?)");

            for (User user : users) {
                pstmt.setString(1, user.getLogin());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getNick());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }
}
