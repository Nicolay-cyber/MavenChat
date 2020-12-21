package chat.server;

import javax.swing.*;
import java.sql.*;

public class SQLHandler
{
    private static Connection connection;
    private static Statement statement;


    public static void connect()
    {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server/MavenChat.db");
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect()
    {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String registerUser(String login, String password)
    {
        try{
            ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE login ='" + login + "'");
            if (!rs.next())
            {
                rs = statement.executeQuery("INSERT INTO users (login, password, nickname) VALUES ('" + login + "', '" + password + "', '" + login + "')");
                return "userCreated";
            }

        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
    public static String getNickByLoginAndPassword(String login, String password)
    {
        try {
            ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE login ='" + login + "' AND password = '" + password + "'");
            if (rs.next()) {
                System.out.println(rs.getString("nickname"));
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
