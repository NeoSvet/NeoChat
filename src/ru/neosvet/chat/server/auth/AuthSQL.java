package ru.neosvet.chat.server.auth;

import java.sql.*;

public class AuthSQL implements AuthService {
    private final String DB_PATH = "jdbc:sqlite:users.db";
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
    public String getNickByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            ResultSet rs = stmt.executeQuery(String.format("SELECT password, nick FROM users WHERE login = '%s'", login));
            if(rs.isClosed()) //login is incorrect
                return null;
            String passDB = rs.getString("password");
            if (passDB != null && passDB.equals(password)) {
                nick = rs.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
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
                    new User("user1", "1111", "Борис_Николаевич"),
                    new User("user2", "2222", "Мартин_Некотов"),
                    new User("user3", "3333", "Гендальф_Серый")
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