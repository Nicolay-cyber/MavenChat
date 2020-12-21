package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    public Server()
    {
        try {
            SQLHandler.connect();
            ServerSocket serverSocket = new ServerSocket(8189);
            clients = new Vector<>();
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
        clients.add(client);
    }
    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }
    public void broadcastMsg(String msg) throws IOException {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
    public void sendMsgTo(String companionNickname, String msg) throws IOException {
        for (ClientHandler c : clients) {
            if(c.getNickname().equals(companionNickname))
            c.sendMsg(msg);
        }
    }


}
