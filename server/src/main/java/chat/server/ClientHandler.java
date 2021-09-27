package chat.server;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler
{
    private final Logger logger = Logger.getLogger("");
    private final DataOutputStream toClient;
    private final DataInputStream fromClient;
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket client) throws IOException
    {
        fromClient = new DataInputStream(client.getInputStream());
        toClient = new DataOutputStream(client.getOutputStream());
        new Thread(() ->
        {
            try {
                while (true)
                {
                    String clientStr = fromClient.readUTF();
                    String[] clientMsg = clientStr.split(" ");
                    if(clientMsg.length >= 3 && clientMsg[0].equals("/userChecking") )
                    {
                        String nickFromDB = SQLHandler.getNickByLoginAndPassword(clientMsg[1], clientMsg[2]);
                        if(nickFromDB != null && !server.isNickInChat(nickFromDB))
                        {
                            nickname = nickFromDB;
                            server.subscribe(this);
                            sendMsg("/UserIsExist " + nickname);
                            logger.log(Level.SEVERE, "Client " + nickname + " is connected");
                            break;
                        }
                        else{
                            logger.log(Level.SEVERE, "There was attempt to connect through " + clientMsg[1] + " login");

                            sendMsg("/UserIsNotExist");
                        }
                    }
                    if(clientMsg.length >= 3 && clientMsg[0].equals("/userRegistration") )
                    {
                        String nickFromDB = SQLHandler.registerUser(clientMsg[1], clientMsg[2]);
                        if(nickFromDB != null)
                        {
                            nickname = nickFromDB;
                            server.subscribe(this);
                            sendMsg("/UserIsExist " + nickname);
                            break;
                        }
                        else{
                            sendMsg("/UserIsNotExist");
                        }
                    }
                    else{
                        sendMsg("/UserIsNotExist");
                        return;
                    }

                }
                while (true)
                {
                    String clientMsg = fromClient.readUTF();
                    if (clientMsg.equals("/end")) {
                        break;
                    }
                    String[] s = clientMsg.split(" ",3);
                    switch (s[0])
                    {
                        case "/loadChat":
                        {
                            server.sendMsgTo(nickname, "/loadDialog " + SQLHandler.loadDialog(s[1], s[2]));
                            break;
                        }
                        case "/saveDialog":
                        {
                            SQLHandler.saveDialog(s[1], s[2], s[3]);
                            break;
                        }

                        case "/changeNicknameAndLogin":
                        {
                            if(SQLHandler.changeNicknameAndLogin(nickname, s[1], s[2]))
                            {
                                server.broadcastMsg("/reloadUserList " + nickname + " " + s[1]);
                                server.sendMsgTo(nickname, "/newNicknameAndLogin " + s[1] + " " + s[2]);
                                nickname = s[1];
                                logger.log(Level.SEVERE, "Client " + nickname + " had changed login or nickname");

                            }
                            break;
                        }
                        case "/changePassword":
                        {
                            if(SQLHandler.changePassword(nickname, s[1], s[2]))
                            {
                                JOptionPane.showMessageDialog(null, "Password is changed");
                                logger.log(Level.SEVERE, "Client " + nickname + " had changed password");
                            }
                            break;

                        }
                        case "/w":
                        {
                            server.sendMsgTo(s[1], "From " + nickname + ": " + s[2]);
                            server.sendMsgTo(nickname, "To " + s[1] + ": " + s[2]);
                            logger.log(Level.SEVERE, "Client " + nickname + " sent message to " + s[1]);

                            break;
                        }
                        case "/getClientList":
                        {
                            String nicknamesList = SQLHandler.getAllNicknames();
                            server.sendMsgTo(nickname, "/ClientList " + nicknamesList);
                            break;
                        }
                        default:
                        {
                            server.broadcastMsg(nickname + ": " + clientMsg);
                            logger.log(Level.SEVERE, "Client " + nickname + " sent message to everyone");
                        }
                    }


                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Client " + nickname + " is disconnected");
            }
            finally {
                closeAllStreams(client);
                server.unsubscribe(this);
            }
        }).start();
    }

    private void closeAllStreams(Socket client) {
        try {
            fromClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            toClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg(String msg)  {
        try {
            toClient.writeUTF(msg);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Message sending error");
            e.printStackTrace();
        }
    }
}
