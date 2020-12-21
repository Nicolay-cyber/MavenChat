package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
                    if(clientMsg.length >= 3 && clientMsg[0].equals("/userChecking") )
                    {
                        String nickFromDB = SQLHandler.getNickByLoginAndPassword(clientMsg[1], clientMsg[2]);
                        if(nickFromDB != null && !server.isNickInChat(nickFromDB))
                        {
                            sendMsg("/UserIsExist");
                            nickname = nickFromDB;
                            server.subscribe(this);
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
                            sendMsg("/UserIsExist");
                            nickname = nickFromDB;
                            server.subscribe(this);
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
                        case "/changeNickname":
                        {
                            SQLHandler.changeNickname(nickname, s[1]);
                            String nicknamesList = SQLHandler.getAllNicknames();
                            server.broadcastMsg("/reloadUserList " + nicknamesList);
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
