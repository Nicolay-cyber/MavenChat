package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private Map<String, ClientHandler> clients;
    public Server()
    {
        try {
            SQLHandler.connect();
            ServerSocket serverSocket = new ServerSocket(8189);
            clients = new ConcurrentHashMap<>();
            while(true)
            {
                Socket socket = serverSocket.accept();
                System.out.println("Client is connected");
                ClientHandler client = new ClientHandler(this,socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            SQLHandler.disconnect();
        }
    }
    public void subscribe(ClientHandler client) {
        clients.put(client.getNickname(), client);
    }
    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNickname());
    }
    public void broadcastMsg(String msg)
    {
        for (ClientHandler c : clients.values()) {
            c.sendMsg(msg);
        }
    }
    public void sendMsgTo(String companionNickname, String msg)
    {
        for (ClientHandler c : clients.values()) {
            if(c.getNickname().equals(companionNickname))
            c.sendMsg(msg);
        }
    }


}
