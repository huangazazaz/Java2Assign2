import javafx.application.Application;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Login extends Application {
    Label yourName = new Label();
    Label yourScore = new Label();
    Label win_times = new Label();
    Game game;
    String username;
    String password;
    BackgroundFill backgroundFill = new BackgroundFill(Paint.valueOf("#708090"), new CornerRadii(20), Insets.EMPTY);
    Background background = new Background(backgroundFill);

    Font textFont = new Font(Font.getDefault().getName(), 20);
    Font buttonFont = new Font(Font.getDefault().getName(), 20);
    Font enterFont = new Font("华文新魏", 23);
    Socket s;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");
        primaryStage.getIcons().add(new Image("file:image/sample.png"));
//    primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.setResizable(false);

        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundImage(new Image("file:image/sample.png"), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(500, 500, false, false, false, false))));


        Button register = new Button("Register");
        Button login = new Button("Login");
//    Button forgot = new Button("Forgot");
//    Button tourist = new Button("Tourist");
        buttonInitial(register, 65, 250, 150, 75);
        buttonInitial(login, 270, 250, 150, 75);
//    buttonInitial(forgot, 65, 345, 150, 75);
//    buttonInitial(tourist, 270, 345, 150, 75);
        addListener(register);
        addListener(login);
//    addListener(forgot);
//    addListener(tourist);


        Text username = new Text("Username");
        Text password = new Text("Password");
        username.setX(75);
        username.setY(70);
        username.setFont(textFont);
        password.setX(75);
        password.setY(180);
        password.setFont(textFont);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        usernameField.setLayoutX(200);
        usernameField.setLayoutY(55);
        usernameField.setPrefWidth(200);
        usernameField.setPromptText("Please input your username");
        passwordField.setLayoutX(200);
        passwordField.setLayoutY(160);
        passwordField.setPrefWidth(200);
        passwordField.setPromptText("Please input your password");
        register.setOnAction(event -> {
            try {
                register();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        login.setOnAction(event -> login(usernameField, passwordField, primaryStage));

        pane.setOnKeyPressed(event -> {
            if (event.getCode().getName().equals("Enter")) {
                login(usernameField, passwordField, primaryStage);
            }
        });
        pane.getChildren().addAll(register, usernameField, passwordField, login, username, password);
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }


    void login(TextField usernameField, PasswordField passwordField, Stage primaryStage) {
        try {
            s = new Socket(InetAddress.getLocalHost(), 2000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Message login = new Message(Message.Type.LOGIN);
        username = usernameField.getText();
        password = passwordField.getText();
        if (username.equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please enter your account!");
            alert.show();
            return;
        } else if (password.equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please enter your password!");
            alert.show();
            return;
        }
        login.setFromPlayer(username);
        login.setFrom(usernameField.getText());
        login.setTo(passwordField.getText());
        SocketUtil.send(s, login);
        ClientThread c = new ClientThread();
        c.socket = s;
        c.l = response -> {
            switch (response.getType()) {
                case LOGOUT:
                    Platform.runLater(() -> {
                        try {
                            List<Object> state = new ArrayList<>();
                            state.add(game.name);
                            state.add(game.rival);
                            state.add(game.end);
                            state.add(game.chessBoard);
                            state.add(game.count);
                            state.add(game.TURN);
                            state.add(!game.lock);
                            Message answer = new Message();
                            answer.setType(Message.Type.RENEW);
//                            answer.setFromPlayer(game.rival);
                            answer.setToPlayer(game.name);
                            answer.setContent(state);
                            SocketUtil.send(s, answer);
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setContentText("Your opponent leaves the game abnormally!");
                            alert.setHeaderText("Please wait");
                            alert.setTitle("Wait");
                            game.wait = true;
                            alert.show();
//                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                case REFRESH:
                    Platform.runLater(() -> {
                        yourScore.setText(response.getFrom());
                        win_times.setText(response.getTo());
                    });
                    break;
                case LOGIN:
                    Platform.runLater(() -> {
                                primaryStage.close();
                                yourName.setText(username);
                                yourScore.setText(response.getFrom());
                                win_times.setText(response.getTo());
                                lobby(s, username, response.getFrom(), response.getTo());
                            }
                    );
                    break;
                case BREAK: {
                    Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Break");
                            alert.setHeaderText("Break");
                            alert.setContentText("Sorry, the server has broke");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.get() == ButtonType.OK) {
                                System.exit(0);
                            } else {
                                System.exit(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    Thread.currentThread().stop();
                }
                break;
                case REFUSE:
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Refuse");
                            alert.setContentText("You are in a going game");
                            alert.show();
                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                    break;
                case FIGHT:
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setContentText(response.getFromPlayer() + " want to fight with you");
                            alert.setTitle("Request against");
                            ButtonType buttonTypeOne = new ButtonType("Accept");
                            ButtonType buttonTypeCancel = new ButtonType("Refuse", ButtonBar.ButtonData.CANCEL_CLOSE);
                            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == buttonTypeOne) {
                                Message answer = new Message();
                                answer.setType(Message.Type.FIGHT_SUCCESS);
                                answer.setToPlayer(response.getToPlayer());
                                answer.setFromPlayer(response.getFromPlayer());
                                SocketUtil.send(s, answer);
                                game = new Game();
                                game.name = username;
                                game.rival = response.getFromPlayer();
                                game.lock = true;
                                game.initial(response.getToPlayer(), response.getFromPlayer());
                                game.start(s, response.getToPlayer(), response.getFromPlayer());
                            } else {
                                Message answer = new Message();
                                answer.setType(Message.Type.FIGHT_FAILURE);
                                answer.setFromPlayer(response.getFromPlayer());
                                answer.setToPlayer(username);
                                SocketUtil.send(s, answer);
                            }
                            this.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                    break;
                case PLAYER: {
                    List<String> player = (List<String>) response.getContent();
                    if (player.size() == 1) {
                        new Thread(() -> Platform.runLater(() -> {
                            try {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("None");
                                alert.setContentText("There is not another player to match");
                                alert.show();
                                stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })).start();
                    } else {
                        player.remove(username);
                        new Thread(() -> Platform.runLater(() -> {
                            try {
                                playerList(player);
                                stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })).start();
                    }
                }
                break;
                case FIGHT_FAILURE:
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Refuse");
                            alert.setContentText(response.getToPlayer() + " refuse your request");
                            alert.show();
                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                    break;
                case FIGHT_SUCCESS: {
                    game = new Game();
                    game.lock = false;
                    game.name = username;
                    game.rival = response.getToPlayer();
                    game.initial(response.getFromPlayer(), response.getToPlayer());
                    game.start(s, response.getFromPlayer(), response.getToPlayer());
                }
                break;
                case PLANT: {
                    game.chessBoard[response.getX()][response.getY()] = game.TURN ? game.PLAY_1 : game.PLAY_2;
                    game.count++;
                    game.TURN = !game.TURN;
                    game.lock = false;
                    game.drawChess();
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            game.ifWin(response.getFromPlayer(), false);
                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                }
                break;
                case FAILURE: {
                    game.record(username);
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setContentText("You win the game!");
                            alert.setHeaderText("Your rival has escaped");
                            alert.setTitle("win");
                            alert.show();
                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                }
                break;
                case WAIT: {
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setContentText("Your opponent leaves the game!");
                            alert.setHeaderText("Please wait");
                            alert.setTitle("Wait");
                            game.wait = true;
                            alert.show();
                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                }
                break;
                case RENEW: {
                    if (response.getFromPlayer().equals(username)) {
                        List<Object> st = (List<Object>) response.getContent();
                        game = new Game();
                        game.rival = (String) st.get(0);
                        game.name = (String) st.get(1);
                        game.end = (boolean) st.get(2);
                        game.chessBoard = (int[][]) st.get(3);
                        game.count = (int) st.get(4);
                        game.TURN = (boolean) st.get(5);
                        game.lock = (boolean) st.get(6);
                        game.initial(game.name, game.rival);
                        game.drawChess();
                        game.start(s, game.name, game.rival);
                    } else {
                        game.wait = false;
                        new Thread(() -> Platform.runLater(() -> {
                            try {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setContentText("Your opponent is back in the game");
                                alert.setHeaderText("Please continue");
                                alert.setTitle("Continue");
                                alert.show();
                                stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })).start();
                    }
                    break;
                }
                case WRONG: {
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setContentText("Password is wrong!");
                            alert.show();
                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                    break;
                }
                case NONEXISTENCE: {
                    new Thread(() -> Platform.runLater(() -> {
                        try {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setContentText("This account is not exist!");
                            alert.show();
                            stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();
                    break;
                }
            }
        };
        c.start();
    }

    void lobby(Socket s, String username, String score, String winTimes) {
        Stage lobby = new Stage();
        lobby.setResizable(false);
        lobby.setWidth(300);
        lobby.setHeight(300);
        lobby.getIcons().add(new Image("file:image/sample.png"));


        Label name = new Label("Name");
        Label Session = new Label("Session");
        Label win = new Label("Win time");

        name.setLayoutX(35);
        name.setLayoutY(20);
        name.setFont(new Font("华文正楷", 16));
        Session.setLayoutX(35);
        Session.setLayoutY(50);
        Session.setFont(new Font("华文正楷", 16));
        win.setLayoutX(35);
        win.setLayoutY(80);
        win.setFont(new Font("华文正楷", 16));
        yourName.setLayoutX(120);
        yourName.setLayoutY(20);
        yourName.setFont(new Font("华文正楷", 16));
        yourScore.setLayoutX(120);
        yourScore.setLayoutY(50);
        yourScore.setFont(new Font("华文正楷", 16));
        win_times.setLayoutX(120);
        win_times.setLayoutY(80);
        win_times.setFont(new Font("华文正楷", 16));


        Button refresh = new Button("Refresh");
        buttonInitial(refresh, 150, 50, 100, 20);
        refresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Message refresh = new Message();
                refresh.setFromPlayer(username);
                refresh.setType(Message.Type.REFRESH);
                SocketUtil.send(s, refresh);
            }
        });


        Button list = new Button("Player to\nMatch");
        buttonInitial(list, 40, 130, 200, 80);
        list.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Message request = new Message();
                request.setType(Message.Type.PLAYER);
                request.setFrom(username);
                SocketUtil.send(s, request);
            }
        });

        Pane account = new Pane();
        account.getChildren().addAll(name, Session, win, list, win_times, yourScore, yourName, refresh);
        lobby.setOnCloseRequest(event -> {
            if (game != null && !game.end) {
                new Thread(() -> Platform.runLater(() -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setContentText("Do you want to quit game?");
                        alert.setTitle("Quit");
                        ButtonType buttonTypeOne = new ButtonType("Quit");
                        ButtonType buttonTypeCancel = new ButtonType("Wait", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == buttonTypeOne) {
                            Message message = new Message();
                            message.setType(Message.Type.FAILURE);
                            message.setFromPlayer(game.name);
                            message.setToPlayer(game.rival);
                            SocketUtil.send(s, message);
                            try {
                                game.record(game.rival);
                                System.exit(0);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            List<Object> state = new ArrayList<>();
                            state.add(game.rival);
                            state.add(game.name);
                            state.add(game.end);
                            state.add(game.chessBoard);
                            state.add(game.count);
                            state.add(game.TURN);
                            state.add(game.lock);
                            Message answer = new Message();
                            answer.setType(Message.Type.WAIT);
                            answer.setFromPlayer(game.name);
                            answer.setToPlayer(game.rival);
                            answer.setContent(state);
                            SocketUtil.send(s, answer);
                            System.exit(0);
                        }
                        Thread.currentThread().stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })).start();
            } else {
                Message request = new Message();
                request.setType(Message.Type.LOGOUT);
                request.setFromPlayer(username);
                SocketUtil.send(s, request);
                System.exit(0);
            }
        });

        Scene scene = new Scene(account);
        lobby.setScene(scene);
        lobby.show();
    }

    void playerList(List<String> players) {
        Stage list = new Stage();
        ListView<String> playerss = new ListView<>();
        playerss.setItems(FXCollections.observableArrayList(players));
        NoticeListItemChangeListener me = new NoticeListItemChangeListener();
        me.myself = s;
        me.myName = username;
        playerss.getSelectionModel().selectedItemProperty().addListener(me);
        ScrollPane player = new ScrollPane();
        player.setContent(playerss);
        Scene scene = new Scene(player);
        list.setScene(scene);
        list.show();
    }

    void register() {
        Stage register = new Stage();
        register.setWidth(300);
        register.setHeight(200);
        register.setResizable(false);
        Pane r = new Pane();
        Button button = new Button("Register");
        buttonInitial(button, 70, 100, 150, 50);
        button.setBackground(Background.EMPTY);
        Label nameLabel = new Label("user name");
        Label passwordLabel = new Label("password");
        PasswordField passwordTextField = new PasswordField();
        TextField nameField = new TextField();
        nameField.setPromptText("Input your user name");
        passwordTextField.setPromptText("Input your password");
        nameLabel.setLayoutX(20);
        nameLabel.setLayoutY(30);
        passwordLabel.setLayoutX(20);
        passwordLabel.setLayoutY(70);
        nameField.setLayoutX(90);
        nameField.setLayoutY(30);
        passwordTextField.setLayoutX(90);
        passwordTextField.setLayoutY(70);
        addListener(button);
        button.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                String name = nameField.getText();
                String password = passwordTextField.getText();
                if (name.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Please enter your name!");
                    alert.show();
                    return;
                } else if (name.length() > 10) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("The length of the user name cannot be greater than 10!");
                    alert.show();
                    return;
                } else if (name.contains(" ")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Name can not contain spacing!");
                    alert.show();
                    return;
                } else if (password.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Please input you password!");
                    alert.show();
                    return;
                } else if (password.length() > 15) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("The length of the password cannot be greater than 15!");
                    alert.show();
                    return;
                } else if (password.contains(" ")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Password can not contain spacing!");
                    alert.show();
                    return;
                }
                File file = new File("src\\Users.txt");
                if (file.exists()) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        ArrayList<String> lines = new ArrayList<>();
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                            String[] info = line.split(" ");
                            if (info[0].equals(name)) {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setContentText("This name has been used!");
                                alert.show();
                                return;
                            }
                        }
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        for (String item : lines) {
                            writer.write(item + "\n");
                        }
                        String info = name + " " + password + " " + 0 + " " + 0 + "\n";
                        writer.write(info);
                        writer.close();
                        register.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        file.createNewFile();
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        String info = name + " " + password + " " + 0 + " " + 0 + "\n";
                        writer.write(info);
                        register.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        r.setOnKeyPressed(event -> {
            if (event.getCode().getName().equals("Enter")) {
                String name = nameField.getText();
                String password = passwordTextField.getText();
                if (name.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Please enter your name!");
                    alert.show();
                    return;
                } else if (name.length() > 10) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("The length of the user name cannot be greater than 10!");
                    alert.show();
                    return;
                } else if (name.contains(" ")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Name can not contain spacing!");
                    alert.show();
                    return;
                } else if (password.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Please input you password!");
                    alert.show();
                    return;
                } else if (password.length() > 15) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("The length of the password cannot be greater than 15!");
                    alert.show();
                    return;
                } else if (password.contains(" ")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Password can not contain spacing!");
                    alert.show();
                    return;
                }
                File file = new File("D:\\idea\\assign2\\src\\Users.txt");
                if (file.exists()) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        ArrayList<String> lines = new ArrayList<>();
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                            String[] info = line.split(" ");
                            if (info[0].equals(name)) {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setContentText("This name has been used!");
                                alert.show();
                                return;
                            }
                        }
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        for (String item : lines) {
                            writer.write(item + "\n");
                        }
                        String info = name + " " + password + " " + 0 + " " + 0 + "\n";
                        writer.write(info);
                        writer.close();
                        register.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        file.createNewFile();
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        String info = name + " " + password + " " + 0 + " " + 0 + "\n";
                        writer.write(info);
                        register.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        Scene s = new Scene(r);
        r.getChildren().addAll(button, nameLabel, passwordLabel, nameField, passwordTextField);
        register.setScene(s);
        register.show();
    }

    void buttonInitial(Button button, int x, int y, int width, int height) {
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        button.setFont(buttonFont);
        button.setBackground(background);
    }

    void addListener(Button button) {
        button.setOnMouseEntered(new EventHandler() {

            @Override
            public void handle(Event event) {
                button.setFont(enterFont);
            }
        });
        button.setOnMouseExited((EventHandler) event -> button.setFont(buttonFont));
    }

    static class NoticeListItemChangeListener implements ChangeListener<Object> {
        Socket myself;
        String myName;

        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            Message fight = new Message();
            fight.setType(Message.Type.FIGHT);
            fight.setToPlayer(newValue.toString());
            fight.setFromPlayer(myName);
            SocketUtil.send(myself, fight);
        }

    }


}

class Game implements Serializable {
    String rival;
    boolean wait = false;
    boolean end = false;
    String name;
    int[][] chessBoard = new int[3][3];
    boolean[][] flag = new boolean[3][3];
    int count = 0;
    int PLAY_1 = 1;
    int PLAY_2 = 2;
    int EMPTY = 0;
    static final int BOUND = 90;
    static final int OFFSET = 15;
    boolean TURN = true;
    Pane base_square = new Pane();

    public Boolean lock;
    Stage primaryStage;
    Socket soc;

    public void initial(String from, String to) {
        new Thread(() -> Platform.runLater(() -> {
            primaryStage = new Stage();
            primaryStage.setTitle(from + "' Tic Tac Toe");
            primaryStage.getIcons().add(new Image("file:image\\sample.png"));
            base_square = new Pane();
            base_square.setPrefWidth(300);
            base_square.setPrefHeight(300);
            Line s1 = new Line();
            Line s2 = new Line();
            Line h1 = new Line();
            Line h2 = new Line();
            s1.setStartX(105);
            s1.setEndX(105);
            s1.setStartY(0);
            s1.setEndY(300);
            s1.setStroke(Color.GRAY);
            s2.setStartX(195);
            s2.setEndX(195);
            s2.setStartY(0);
            s2.setEndY(300);
            s2.setStroke(Color.GRAY);
            h1.setStartX(0);
            h1.setEndX(300);
            h1.setStartY(105);
            h1.setEndY(105);
            h1.setStroke(Color.GRAY);
            h2.setStartX(0);
            h2.setEndX(300);
            h2.setStartY(195);
            h2.setEndY(195);
            h2.setStroke(Color.GRAY);
            base_square.getChildren().addAll(h1, h2, s2, s1);
            Scene scene = new Scene(base_square);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            Thread.currentThread().stop();
        })).start();
    }

    public void start(Socket socket, String from, String to) {
        soc = socket;
        new Thread(() -> Platform.runLater(() -> {
            try {
                primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        if (!end) {
                            new Thread(() -> Platform.runLater(() -> {
                                try {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setContentText("Do you want to quit game?");
                                    alert.setTitle("Quit");
                                    ButtonType buttonTypeOne = new ButtonType("Quit");
                                    ButtonType buttonTypeCancel = new ButtonType("Wait", ButtonBar.ButtonData.CANCEL_CLOSE);
                                    alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
                                    Optional<ButtonType> result = alert.showAndWait();
                                    if (result.isPresent() && result.get() == buttonTypeOne) {
                                        Message message = new Message();
                                        message.setType(Message.Type.FAILURE);
                                        message.setFromPlayer(name);
                                        message.setToPlayer(rival);
                                        SocketUtil.send(soc, message);
                                        try {
                                            record(rival);
                                            System.exit(0);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        List<Object> state = new ArrayList<>();
                                        state.add(rival);
                                        state.add(name);
                                        state.add(end);
                                        state.add(chessBoard);
                                        state.add(count);
                                        state.add(TURN);
                                        state.add(lock);
                                        Message answer = new Message();
                                        answer.setType(Message.Type.WAIT);
                                        answer.setFromPlayer(name);
                                        answer.setToPlayer(rival);
                                        answer.setContent(state);
                                        SocketUtil.send(soc, answer);
                                        System.exit(0);
                                    }
                                    Thread.currentThread().stop();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })).start();
                        }
                    }
                });
//                    game_panel = new Rectangle();
                base_square.setOnMouseClicked(event -> {
                    if (!lock && !wait) {
                        int x = (int) (event.getX() / BOUND);
                        int y = (int) (event.getY() / BOUND);
                        try {
                            if (refreshBoard(x, y)) {
                                TURN = !TURN;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (x >= 0 && x < 3 && y >= 0 && y < 3 && !flag[x][y]) {
                            Message message = new Message();
                            message.setX(x);
                            message.setY(y);
                            message.setType(Message.Type.PLANT);
                            message.setFromPlayer(from);
                            message.setToPlayer(to);
                            SocketUtil.send(soc, message);
                            lock = true;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    private void drawCircle(int i, int j) {
        new Thread(() -> Platform.runLater(() -> {
            try {
                Circle circle = new Circle();
                base_square.getChildren().add(circle);
                circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
                circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
                circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
                circle.setStroke(Color.RED);
                circle.setFill(Color.TRANSPARENT);
                flag[i][j] = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    private void drawLine(int i, int j) {
        new Thread(() -> Platform.runLater(() -> {
            try {
                Line line_a = new Line();
                Line line_b = new Line();
                base_square.getChildren().add(line_a);
                base_square.getChildren().add(line_b);
                line_a.setStartX(i * BOUND + OFFSET * 1.5);
                line_a.setStartY(j * BOUND + OFFSET * 1.5);
                line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
                line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
                line_a.setStroke(Color.BLUE);
                line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
                line_b.setStartY(j * BOUND + OFFSET * 1.5);
                line_b.setEndX(i * BOUND + OFFSET * 1.5);
                line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
                line_b.setStroke(Color.BLUE);
                flag[i][j] = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }


    private boolean refreshBoard(int x, int y) throws IOException {
        if (x >= 0 && x < 3 && y >= 0 && y < 3 && !flag[x][y]) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            count++;
            drawChess();
            ifWin(name, true);
            return true;
        }
        return false;
    }

    void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case 1:
                        drawCircle(i, j);
                        break;
                    case 2:
                        drawLine(i, j);
                        break;
                }
            }
        }
    }

    void ifWin(String winner, boolean win) throws IOException {
        if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][0] && chessBoard[1][0] == chessBoard[2][0]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            alert.show();
            record(winner);
//                Platform.exit();
        } else if (chessBoard[0][1] != 0 && chessBoard[0][1] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][1]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            alert.show();
            record(winner);
//                Platform.exit();
        } else if (chessBoard[0][2] != 0 && chessBoard[0][2] == chessBoard[1][2] && chessBoard[1][2] == chessBoard[2][2]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            record(winner);
            alert.show();
//                Platform.exit();
        } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[0][1] && chessBoard[0][1] == chessBoard[0][2]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            record(winner);
            alert.show();
//                Platform.exit();
        } else if (chessBoard[1][0] != 0 && chessBoard[1][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[1][2]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            record(winner);
            alert.show();
//                Platform.exit();
        } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[2][1] && chessBoard[2][1] == chessBoard[2][2]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            record(winner);
            alert.show();
//                Platform.exit();
        } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][2]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            alert.show();
//                Platform.exit();
            record(winner);
        } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[0][2]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Winner is " + winner);
            alert.setHeaderText(win ? "You win the game" : "You lose the game");
            alert.setTitle("win");
            alert.show();
//                Platform.exit();
            record(winner);
        } else if (count == 9) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("This is a tie!");
            alert.setHeaderText("tie!");
            alert.setTitle("tie");
            alert.show();
//                Platform.exit();
            winner = "";
            record(winner);
        }
    }

    void record(String winner) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src\\Users.txt"));
        List<String> information = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] info = line.split(" ");
            if (info[0].equals(name)) {
                info[2] = String.valueOf(Integer.parseInt(info[2]) + 1);
                if (name.equals(winner)) {
                    info[3] = String.valueOf(Integer.parseInt(info[3]) + 1);
                }
                information.add(info[0] + " " + info[1] + " " + info[2] + " " + info[3] + "\n");
            } else {
                information.add(line + "\n");
            }
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter("src\\Users.txt"));
        for (String info : information) {
            writer.write(info);
        }
        writer.close();
        lock = true;
        end = true;
        Message end = new Message();
        end.setType(Message.Type.END);
        end.setFromPlayer(name);
        SocketUtil.send(soc, end);
//      primaryStage.close();
    }
}

