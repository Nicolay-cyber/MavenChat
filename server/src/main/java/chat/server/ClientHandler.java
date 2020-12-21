package chat.server;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler {
    private Server server;
    private Socket socket;

    private final DataOutputStream toClient;
    private final DataInputStream fromClient;
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket client) throws IOException
    {
        this.server = server;
        this.socket = client;
        fromClient = new DataInputStream(client.getInputStream());
        toClient = new DataOutputStream(client.getOutputStream());
        new Thread(() ->
        {
            try {
                while (true)
                {
                    String clientStr = fromClient.readUTF();
                    String[] clientMsg = clientStr.split(" ");
                    if(clientMsg.length >= 3 && clientMsg[0].equals("/userChecking"))
                    {
                        String nickFromDB = SQLHandler.getNickByLoginAndPassword(clientMsg[1], clientMsg[2]);
                        if(nickFromDB != null)
                        {
                            sendMsg("/UserIsExist");
                            server.subscribe(this);
                            nickname = nickFromDB;
                            break;
                        }
                        else{
                            sendMsg("/UserIsNotExist");
                        }
                    }
                    else if(clientMsg.length >= 3 && clientMsg[0].equals("/userRegistration"))
                    {
                        String nickFromDB = SQLHandler.registerUser(clientMsg[1], clientMsg[2]);
                        if(nickFromDB != null)
                        {
                            sendMsg("/UserIsExist");
                            server.subscribe(this);
                            nickname = nickFromDB;
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
                    System.out.println(clientMsg);
                    if (clientMsg.equals("/end")) {
                        break;
                    }
                    if(clientMsg.contains("/w"))
                    {
                        String[] s = clientMsg.split(" ");
                        if(s.length >= 3 && s[0].equals("/w"))
                        {
                            server.sendMsgTo(s[1], nickname + ":" + clientMsg.substring(s[1].length() + 3));
                        }
                    }
                    else{
                        server.broadcastMsg(nickname + ": " + clientMsg);

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


    public void sendMsg(String msg) throws IOException {
        toClient.writeUTF(msg);
    }
}
