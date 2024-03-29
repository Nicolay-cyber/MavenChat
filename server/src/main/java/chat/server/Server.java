package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Server {
    private Map<String, ClientHandler> clients;
    private static final Logger logger = Logger.getLogger("");

    public Server()
    {
        logger.getHandlers()[0].setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getLevel() + ": " + record.getMessage() + " " + record.getMillis() + "\n";
            }
        });
        try {
            SQLHandler.connect();
            ServerSocket serverSocket = new ServerSocket(8189);
            clients = new ConcurrentHashMap<>();
            while(true)
            {
                Socket socket = serverSocket.accept();
                new ClientHandler(this,socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Client connection error");
        }
        finally {
            SQLHandler.disconnect();
        }
    }
    public void subscribe(ClientHandler client) {
        clients.put(client.getNickname(), client);
    }
    public boolean isNickInChat(String nickname) {
        return clients.containsKey(nickname);
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
            {
                c.sendMsg(msg);
            }
        }
    }


}
