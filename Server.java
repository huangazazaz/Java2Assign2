import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    static HashMap<String, Socket> players = new HashMap<>();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(2000);
            System.out.println("success");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress().getHostName());
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ServerThread extends Thread {
        private Socket socket;

        public ServerThread() {
        }

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        public Socket getSocket() {
            return socket;
        }

        public void setSocket(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                Object receive = SocketUtil.receive(socket);
                if (receive instanceof Message) {
                    Message request = (Message) receive;
                    System.out.println(request.getType());
                    switch (request.getType()) {
                        case PLAYER:
                            Message message = new Message();
                            List<String> p = new LinkedList<>(players.keySet());
                            message.setContent(p);
                            message.setType(Message.Type.PLAYER);
                            SocketUtil.send(socket, message);
                            break;
                        case LOGIN:
                            players.put(request.getFromPlayer(), socket);
                            break;
                        case LOGOUT:
                            players.remove(request.getFromPlayer());
                            this.stop();
                            break;
                        case FIGHT:
                            Message fight = new Message();
                            fight.setType(Message.Type.FIGHT);
                            fight.setFromPlayer(request.getFromPlayer());
                            SocketUtil.send(players.get(request.getToPlayer()), fight);
                            break;
                        case FIGHT_SUCCESS:
                            Message start = new Message();
                            start.setType(Message.Type.SUCCESS);
                            SocketUtil.send(players.get(request.getFromPlayer()),start);
                            break;
                        case FIGHT_FAILURE:
                            Message result = new Message();
                            result.setType(Message.Type.FIGHT_FAILURE);
                            result.setToPlayer(request.getToPlayer());
                            SocketUtil.send(players.get(request.getFromPlayer()),result);
                            break;
                    }
                }
            }
        }
    }
}

