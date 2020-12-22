package chat.server;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
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
                            break;
                        }
                        else{
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
                        case "/changeNicknameAndLogin":
                        {
                            if(SQLHandler.changeNicknameAndLogin(nickname, s[1], s[2]))
                            {
                                server.broadcastMsg("/reloadUserList " + nickname + " " + s[1]);
                                server.sendMsgTo(nickname, "/newNicknameAndLogin " + s[1] + " " + s[2]);
                                nickname = s[1];
                            }
                            break;
                        }
                        case "/changePassword":
                        {
                            if(SQLHandler.changePassword(nickname, s[1], s[2]))
                            {
                                JOptionPane.showMessageDialog(null, "Password is changed");
                            }
                            break;

                        }
                        case "/w":
                        {
                                server.sendMsgTo(s[1], "From " + nickname + ": " + s[2]);
                                server.sendMsgTo(nickname, "To " + s[1] + ": " + s[2]);
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
                        }
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
