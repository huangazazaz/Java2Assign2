import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.FileReader;
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
    static HashMap<String, String> threads = new HashMap<>();
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
                    switch (request.getType()) {
                        case RENEW:
                            for (Battle item : battles) {
                                if (item.player1.equals(request.getToPlayer()) || item.player2.equals(request.getToPlayer())) {
                                    item.state = (List<Object>) request.getContent();
                                    break;
                                }
                            }
                            break;
                        case BREAK:
                            for (String a : threads.keySet()) {
                                if (threads.get(a).equals(this.getName())) {
                                    boolean ing = false;
                                    for (Battle battle : battles) {
                                        if (battle.player2.equals(a) || battle.player1.equals(a)) {
                                            ing = true;
                                            Message b = new Message(Message.Type.LOGOUT);
                                            b.setFromPlayer(a.equals(battle.player1) ? battle.player1 : battle.player2);
                                            SocketUtil.send(players.get(a.equals(battle.player2) ? battle.player1 : battle.player2), b);
                                            System.out.println(a + " exit the game abnormally and wait to back");
                                            break;
                                        }
                                    }
                                    players.remove(a);
                                    threads.remove(a);
                                    if (!ing) {
                                        System.out.println(a + " exit the game abnormally");
                                    }
                                    break;
                                }
                            }
                            this.stop();
                            break;
                        case PLAYER:
                            boolean check = true;
                            System.out.println(request.getFrom() + " want to check the player list");
                            for (Battle i : battles) {
                                if (i.player1.equals(request.getFrom()) || i.player2.equals(request.getFrom())) {
                                    Message message = new Message();
                                    message.setType(Message.Type.REFUSE);
                                    SocketUtil.send(socket, message);
                                    check = false;
                                    break;
                                }
                            }
                            if (check) {
                                List<String> p = new LinkedList<>(players.keySet().stream().filter(x -> {
                                    for (Battle b : battles) {
                                        if (x.equals(b.player1) || x.equals(b.player2)) {
                                            return false;
                                        }
                                    }
                                    return true;
                                }).collect(Collectors.toList()));
                                Message message = new Message();
                                message.setContent(p);
                                message.setType(Message.Type.PLAYER);
                                SocketUtil.send(socket, message);
                            }
                            break;
                        case LOGIN:
                            try {
                                String Username = request.getFrom();
                                String Password = request.getTo();
                                BufferedReader reader = new BufferedReader(new FileReader("src\\Users.txt"));
                                String line;
                                Message req = new Message();
                                boolean exit = false;
                                while ((line = reader.readLine()) != null) {
                                    String[] info = line.split(" ");
                                    if (info[0].equals(Username)) {
                                        exit = true;
                                        if (info[1].equals(Password)) {
                                            req.setType(Message.Type.LOGIN);
                                            req.setFrom(info[2]);
                                            req.setTo(info[3]);
                                            System.out.println(request.getFromPlayer() + " login");
                                            players.put(request.getFromPlayer(), socket);
                                            threads.put(request.getFromPlayer(), this.getName());
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
                                        } else {
                                            req.setType(Message.Type.WRONG);
                                        }
                                        SocketUtil.send(socket, req);
                                        break;
                                    }
                                }
                                if (!exit) {
                                    req.setType(Message.Type.NONEXISTENCE);
                                    SocketUtil.send(socket, req);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            break;
                        case REFRESH:
                            System.out.println(request.getFromPlayer() + " refresh record");
                            Message refresh = new Message(Message.Type.REFRESH);
                            try {
                                BufferedReader reader = new BufferedReader(new FileReader("src\\Users.txt"));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    String[] info = line.split(" ");
                                    if (info[0].equals(request.getFromPlayer())) {
                                        refresh.setFrom(info[2]);
                                        refresh.setTo(info[3]);
                                        SocketUtil.send(socket, refresh);
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case LOGOUT:
                            System.out.println(request.getFromPlayer() + " logout");
                            for (Battle item : battles) {
                                if (item.player1.equals(request.getFromPlayer()) || item.player2.equals(request.getFromPlayer())) {
                                    battles.remove(item);
                                    break;
                                }
                            }
                            players.remove(request.getFromPlayer());
                            threads.remove(request.getFromPlayer());
                            this.stop();
                            break;
                        case FIGHT:
                            System.out.println(request.getFromPlayer() + " send a fight request to " + request.getToPlayer());
                            Message fight = new Message();
                            fight.setType(Message.Type.FIGHT);
                            fight.setFromPlayer(request.getFromPlayer());
                            fight.setToPlayer(request.getToPlayer());
                            SocketUtil.send(players.get(request.getToPlayer()), fight);
                            break;
                        case FIGHT_SUCCESS:
                            System.out.println("fight between " + request.getFromPlayer() + " and " + request.getToPlayer() + " start successful");
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
                            System.out.println("fight fail to start");
                            Message result = new Message();
                            result.setType(Message.Type.FIGHT_FAILURE);
                            result.setToPlayer(request.getToPlayer());
                            SocketUtil.send(players.get(request.getFromPlayer()), result);
                            break;
                        case PLANT:
                            System.out.println(request.getFromPlayer() + " plant in " + request.getX() + " " + request.getY());
                            Message to = new Message();
                            to.setType(Message.Type.PLANT);
                            to.setX(request.getX());
                            to.setY(request.getY());
                            to.setFromPlayer(request.getFromPlayer());
                            SocketUtil.send(players.get(request.getToPlayer()), to);
                            break;
                        case FAILURE:
                            System.out.println(request.getFromPlayer() + " escape the game");
                            players.remove(request.getFromPlayer());
                            threads.remove(request.getFromPlayer());
                            SocketUtil.send(players.get(request.getToPlayer()), request);
                            this.stop();
                            break;
                        case WAIT:
                            System.out.println(request.getFromPlayer() + " exit the game and wait to back");
                            Message wait = new Message();
                            wait.setType(Message.Type.WAIT);
                            wait.setFromPlayer(request.getFromPlayer());
                            SocketUtil.send(players.get(request.getToPlayer()), wait);
                            players.remove(request.getFromPlayer());
                            threads.remove(request.getFromPlayer());
                            for (Battle item : battles) {
                                if (item.player1.equals(request.getFromPlayer()) || item.player2.equals(request.getFromPlayer())) {
                                    item.state = (List<Object>) request.getContent();
                                    break;
                                }
                            }
                            this.stop();
                            break;
                        case END:
                            System.out.println("fight is end");
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

