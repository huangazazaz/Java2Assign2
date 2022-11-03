import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
  static class Battle {
    String player1;
    String player2;
    List<Object> state;
  }

  static HashMap<String, Socket> players = new HashMap<>();
  static List<Battle> battles = new ArrayList<>();


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
            case BREAK:
              this.stop();
              break;
            case PLAYER:
              Message message = new Message();
              List<String> p = new LinkedList<>(players.keySet().stream().filter(x -> {
                for (Battle b : battles) {
                  if (x.equals(b.player1) || x.equals(b.player2)) {
                    return false;
                  }
                }
                return true;
              }).collect(Collectors.toList()));
              message.setContent(p);
              message.setType(Message.Type.PLAYER);
              SocketUtil.send(socket, message);
              break;
            case LOGIN:
              players.put(request.getFromPlayer(), socket);
              for (Battle item : battles) {
                if (item.player1.equals(request.getFromPlayer()) || item.player2.equals(request.getFromPlayer())) {
                  Message renew = new Message();
                  renew.setContent(item.state);
                  renew.setType(Message.Type.RENEW);
                  renew.setFromPlayer(item.player1.equals(request.getFromPlayer()) ? item.player1 : item.player2);
                  SocketUtil.send(socket, renew);
                  SocketUtil.send(players.get((item.player1.equals(request.getFromPlayer()) ? item.player2 : item.player1)), renew);
                }
              }
              break;
            case LOGOUT:
              for (Battle item : battles) {
                if (item.player1.equals(request.getFromPlayer()) || item.player2.equals(request.getFromPlayer())) {
                  battles.remove(item);
                  break;
                }
              }
              players.remove(request.getFromPlayer());
              this.stop();
              break;
            case FIGHT:
              Message fight = new Message();
              fight.setType(Message.Type.FIGHT);
              fight.setFromPlayer(request.getFromPlayer());
              fight.setToPlayer(request.getToPlayer());
              SocketUtil.send(players.get(request.getToPlayer()), fight);
              break;
            case FIGHT_SUCCESS:
              Battle battle = new Battle();
              battle.player1 = request.getToPlayer();
              battle.player2 = request.getFromPlayer();
              battles.add(battle);
              Message start = new Message();
              start.setType(Message.Type.FIGHT_SUCCESS);
              start.setFromPlayer(request.getFromPlayer());
              start.setToPlayer(request.getToPlayer());
              SocketUtil.send(players.get(request.getFromPlayer()), start);
              break;
            case FIGHT_FAILURE:
              Message result = new Message();
              result.setType(Message.Type.FIGHT_FAILURE);
              result.setToPlayer(request.getToPlayer());
              SocketUtil.send(players.get(request.getFromPlayer()), result);
              break;
            case PLANT:
              Message to = new Message();
              to.setType(Message.Type.PLANT);
              to.setX(request.getX());
              to.setY(request.getY());
              to.setFromPlayer(request.getFromPlayer());
              SocketUtil.send(players.get(request.getToPlayer()), to);
              break;
            case FAILURE:
              players.remove(request.getFromPlayer());
              SocketUtil.send(players.get(request.getToPlayer()), request);
              this.stop();
              break;
            case WAIT:
              Message wait = new Message();
              wait.setType(Message.Type.WAIT);
              wait.setFromPlayer(request.getFromPlayer());
              SocketUtil.send(players.get(request.getToPlayer()), wait);
              players.remove(request.getFromPlayer());
              for (Battle item : battles){
                if (item.player1.equals(request.getFromPlayer())||item.player2.equals(request.getFromPlayer())){
                  item.state = (List<Object>) request.getContent();
                  break;
                }
              }
              this.stop();
              break;
            case END:
              for (Battle i : battles) {
                if (i.player1.equals(request.getFromPlayer()) || i.player2.equals(request.getFromPlayer())) {
                  battles.remove(i);
                  break;
                }
              }
              break;
          }
        }
      }
    }
  }
}

