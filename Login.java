import javafx.application.Application;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Login extends Application {
    String Username;
    String Password;
    BackgroundFill backgroundFill = new BackgroundFill(Paint.valueOf("#BBBB00"), new CornerRadii(20), Insets.EMPTY);
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
        primaryStage.getIcons().add(new Image("file:/D:/idea/assign2/image/sample.png"));
//    primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.setResizable(false);

        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundImage(new Image("file:/D:/idea/assign2/image/sample.png"), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(500, 500, false, false, false, false))));


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
            Username = usernameField.getText();
            Password = passwordField.getText();
            BufferedReader reader = new BufferedReader(new FileReader("D:\\idea\\assign2\\src\\Users.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(" ");
                if (info[0].equals(Username)) {
                    if (info[1].equals(Password)) {
                        primaryStage.close();
                        Message req = new Message();
                        req.setType(Message.Type.LOGIN);
                        req.setFromPlayer(Username);
                        s = new Socket(InetAddress.getLocalHost(), 2000);
                        SocketUtil.send(s, req);

                        lobby(s, Username, info[2], info[3]);
                        return;
                    } else if (Password.equals("")) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setContentText("Please enter your password!");
                        alert.show();
                        return;
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setContentText("Wrong password!");
                        alert.show();
                        return;
                    }
                }
            }
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("This account dose not exist");
            alert.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    void lobby(Socket s, String username, String score, String winTimes) {
        Stage lobby = new Stage();
        lobby.setResizable(false);
        lobby.setWidth(300);
        lobby.setHeight(300);

        Label name = new Label("Name");
        Label Session = new Label("Session");
        Label win = new Label("Win time");
        Label yourName = new Label(username);
        Label yourScore = new Label(score);
        Label win_times = new Label(winTimes);

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


        Button list = new Button("Online\nPlayers");
        buttonInitial(list, 40, 130, 200, 80);
        list.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {

                Message request = new Message(null, Message.Type.PLAYER, s.getInetAddress().getHostName(), null);
                SocketUtil.send(s, request);
            }
        });

        Pane account = new Pane();
        account.getChildren().addAll(name, Session, win, list, win_times, yourScore, yourName);
        lobby.setOnCloseRequest(event -> {
            Message request = new Message();
            request.setType(Message.Type.LOGOUT);
            request.setFromPlayer(username);
            SocketUtil.send(s, request);
        });

        Scene scene = new Scene(account);
        lobby.setScene(scene);
        lobby.show();
        ClientThread c = new ClientThread();
        c.socket = s;
        c.l = response -> {
            if (response.getType().equals(Message.Type.FIGHT)) {
                new Thread(() -> Platform.runLater(() -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setContentText(response.getFromPlayer() + " want to fight with you");
                        alert.setTitle("Request against");
                        ButtonType buttonTypeOne = new ButtonType("Accept");
                        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == buttonTypeOne) {
                            Message answer = new Message();
                            answer.setType(Message.Type.FIGHT_SUCCESS);
                            answer.setToPlayer(username);
                            answer.setFromPlayer(response.getFromPlayer());
                            SocketUtil.send(s, answer);
                            Game game = new Game();
                            game.lock = true;
                            game.start();
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
            } else if (response.getType().equals(Message.Type.PLAYER)) {
                List<String> player = (List<String>) response.getContent();
                player.remove(username);
                new Thread(() -> Platform.runLater(() -> {
                    try {
                        playerList(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })).start();
            } else if (response.getType().equals(Message.Type.FIGHT_FAILURE)) {
                new Thread(() -> Platform.runLater(() -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Refuse");
                        alert.setContentText(response.getToPlayer() + " refuse your request");
                        alert.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })).start();

            }
        };
        c.start();
    }

    void playerList(List<String> players) {
        Stage list = new Stage();
        ListView<String> playerss = new ListView<>();
        playerss.setItems(FXCollections.observableArrayList(players));
        NoticeListItemChangeListener me = new NoticeListItemChangeListener();
        me.myself = s;
        me.myName = Username;
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
    public static class Game {
        private static final int[][] chessBoard = new int[3][3];
        private static final boolean[][] flag = new boolean[3][3];
        private static int count = 0;
        private static final int PLAY_1 = 1;
        private static final int PLAY_2 = 2;
        private static final int EMPTY = 0;
        private static final int BOUND = 90;
        private static final int OFFSET = 15;
        private static boolean TURN = true;
        private Pane base_square = new Pane();
        private Rectangle game_panel = new Rectangle();

        public Boolean lock;


        public void start() {
            new Thread(() -> Platform.runLater(() -> {
                try {
                    Stage primaryStage = new Stage();
                    primaryStage.setTitle("Tic Tac Toe");
                    primaryStage.getIcons().add(new Image("file:\\D:\\\\idea\\\\assign2\\\\image\\\\sample.png"));
                    base_square = new Pane();
                    base_square.setPrefWidth(300);
                    base_square.setPrefHeight(300);
//                    game_panel = new Rectangle();
                    base_square.setOnMouseClicked(event -> {
                        if (lock) {
                            int x = (int) (event.getX() / BOUND);
                            int y = (int) (event.getY() / BOUND);
                            if (refreshBoard(x, y)) {
                                TURN = !TURN;
                            }
                        }
                    });
                    Scene scene = new Scene(base_square);
                    primaryStage.setScene(scene);
                    primaryStage.setResizable(false);
                    primaryStage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            })).start();
        }

        private void drawCircle(int i, int j) {
            Circle circle = new Circle();
            base_square.getChildren().add(circle);
            circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
            circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
            circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
            circle.setStroke(Color.RED);
            circle.setFill(Color.TRANSPARENT);
            flag[i][j] = true;
        }

        private void drawLine(int i, int j) {
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
        }


        private boolean refreshBoard(int x, int y) {
            if (chessBoard[x][y] == EMPTY) {
                chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
                count++;
                drawChess();
                ifWin();
                return true;
            }
            return false;
        }

        private void drawChess() {
            for (int i = 0; i < chessBoard.length; i++) {
                for (int j = 0; j < chessBoard[0].length; j++) {
                    if (flag[i][j]) {
                        // This square has been drawing, ignore.
                        continue;
                    }
                    switch (chessBoard[i][j]) {
                        case PLAY_1:
                            drawCircle(i, j);
                            break;
                        case PLAY_2:
                            drawLine(i, j);
                            break;
                        case EMPTY:
                            // do nothing
                            break;
                        default:
                            System.err.println("Invalid value!");
                    }
                }
            }
        }

        private void ifWin() {
            if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][0] && chessBoard[1][0] == chessBoard[2][0]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[0][0] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (chessBoard[0][1] != 0 && chessBoard[0][1] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][1]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[0][1] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (chessBoard[0][2] != 0 && chessBoard[0][2] == chessBoard[1][2] && chessBoard[1][2] == chessBoard[2][2]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[0][2] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[0][1] && chessBoard[0][1] == chessBoard[0][2]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[0][0] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (chessBoard[1][0] != 0 && chessBoard[1][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[1][2]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[1][0] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[2][1] && chessBoard[2][1] == chessBoard[2][2]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[2][0] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][2]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[0][0] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[0][2]) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(chessBoard[0][2] == 1 ? "winner is player 1" : "winner is player 2");
                alert.setTitle("win");
                alert.show();
                Platform.exit();
            } else if (count == 9) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("This is a draw!");
                alert.setTitle("draw");
                alert.show();
                Platform.exit();
            }
        }

    }

}

