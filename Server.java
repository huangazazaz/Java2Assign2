import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    HashMap<String, Battle> battles = new HashMap<>();


    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(2000);
            System.out.println("Server start");
            Socket wait = null;
            while (true) {
                Socket accept = serverSocket.accept();
//          Battle battle = new Battle();
//          battles.put(battle.name, battle);
                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            Object receive = SocketUtil.receive(accept);
                            if (receive instanceof Message) {
                                Message request = (Message) receive;
                                if (request.getType().equals(Message.Type.PLAYER)) {
                                    Message message = new Message();
                                    List<String> ps = new ArrayList<>();
                                    ps.add("a");
                                    ps.add("e");
                                    ps.add("d");
                                    ps.add("c");
                                    ps.add("b");
                                    message.setContent(ps);
                                    SocketUtil.send(accept, message);
                                }
                            }
                        }
                    }
                }.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Battle {
    Socket[] players = new Socket[2];

    String name;

    void start() {
        new Thread() {
            @Override
            public void run() {
                while (true) {

                }
            }
        }.start();
    }


    void add(Socket p1, Socket p2) {
        players[0] = p1;
        players[1] = p2;
        name = p1.getInetAddress().getHostName() + p2.getInetAddress().getHostName();
    }
}
