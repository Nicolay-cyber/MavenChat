package chat.server;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLHandler
{
    private static Connection connection;
    private static Statement statement;
    private static Logger logger = Logger.getLogger("");


    public static void connect()
    {
        try {
            String url = "jdbc:postgresql://ec2-34-251-245-108.eu-west-1.compute.amazonaws.com:5432/d5gmno0qk5qjah?sslmode=require";
            String user = "bmdubxlelfebxw";
            String pass = "34730ab8628e7fbc84bf42f311641e81a54d591042e56b1d70dabd61cdeaae74";
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url,user,pass);
            statement = connection.createStatement();
            logger.log(Level.SEVERE, "Database is connected");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database connection error");
            e.printStackTrace();
        }
    }
    public static String loadDialog(String firstParticipant, String secondParticipant)
    {
        try
        {
            ResultSet rs = statement.executeQuery("SELECT " + secondParticipant + " FROM dialogs WHERE firstParticipant = '" + firstParticipant + "'" );
            return rs.getString(secondParticipant);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public static void saveDialog(String firstParticipant, String secondParticipant, String dialog)
    {
        try
        {
            statement.executeUpdate("UPDATE dialogs SET " + secondParticipant + " = '" + dialog + "' WHERE  firstParticipant = '" + firstParticipant + "'");

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static boolean changeNicknameAndLogin(String oldNickname, String newNickname, String newLogin)
    {
        try{
            statement.executeUpdate("UPDATE users SET nickname = '" + newNickname + "', login = '" + newLogin + "' WHERE  nickname = '" + oldNickname + "'");
            return true;
        }
         catch (SQLException e)
        {
         e.printStackTrace();
         return false;
        }
    }
    public static boolean changePassword(String nickname, String oldPassword, String newPassword)
    {
        try{
            ResultSet rs = statement.executeQuery("SELECT password FROM users WHERE nickname = '" + nickname + "'");
            if(rs.getString("password").equals(oldPassword)){
                statement.executeUpdate("UPDATE users SET password = '" + newPassword + "' WHERE  nickname = '" + nickname + "'");
                return true;
            }
            return false;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public static void disconnect()
    {
        try
        {
            logger.log(Level.SEVERE, "Database is disconnected");
            connection.close();
        } catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Database disconnection error");
            e.printStackTrace();
        }
    }
    public static String registerUser(String login, String password)
    {
        try{
                ResultSet rs = statement.executeQuery("INSERT INTO users (login, password, nickname) VALUES ('"
                        + login + "', '" + password + "', '" + login + "')");
                return rs.getString("nickname");
        }
        catch (SQLException e){
            return null;
        }
    }
    public static String getAllNicknames()
    {
        StringBuilder clientList = new StringBuilder();
        try
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM users");
            while(rs.next()){
                clientList.append(rs.getString("nickname") + " ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientList.toString();
    }
    public static String getNickByLoginAndPassword(String login, String password)
    {
        try {
            ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE login ='" + login + "' AND password = '" + password + "'");
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was error while server tried to get Nick by login and password");
            e.printStackTrace();
        }
        return null;
    }

}
