import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class Login extends Application {
  Label yourName = new Label();
  Label yourScore = new Label();
  Label win_times = new Label();
  Game game;
  String username;
  String password;
  BackgroundFill backgroundFill = new BackgroundFill(Paint.valueOf("#708090"),
      new CornerRadii(20), Insets.EMPTY);
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
    primaryStage.setWidth(500);
    primaryStage.setHeight(500);
    primaryStage.setResizable(false);

    Pane pane = new Pane();
    pane.setBackground(new Background(new BackgroundImage(new Image("file:image/sample.png"),
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
        new BackgroundSize(500, 500, false, false, false, false))));


    Button register = new Button("Register");
    Button login = new Button("Login");
    buttonInitial(register, 65, 250, 150, 75);
    buttonInitial(login, 270, 250, 150, 75);
    addListener(register);
    addListener(login);
    Text username = new Text("Username");
    username.setX(75);
    username.setY(70);
    username.setFont(textFont);
    Text password = new Text("Password");
    password.setX(75);
    password.setY(180);
    password.setFont(textFont);
    TextField usernameField = new TextField();
    usernameField.setLayoutX(200);
    usernameField.setLayoutY(55);
    usernameField.setPrefWidth(200);
    usernameField.setPromptText("Please input your username");
    PasswordField passwordField = new PasswordField();
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
    username = usernameField.getText();
    password = passwordField.getText();
    Message login = new Message(Message.Type.LOGIN);
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
              answer.setToPlayer(game.name);
              answer.setContent(state);
              SocketUtil.send(s, answer);
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setContentText("Your opponent leaves the game abnormally!");
              alert.setHeaderText("Please wait");
              alert.setTitle("Wait");
              game.wait = true;
              alert.show();
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
              ButtonType buttonTypeCancel = new ButtonType("Refuse",
                  ButtonBar.ButtonData.CANCEL_CLOSE);
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
        default:break;
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
    name.setLayoutX(35);
    name.setLayoutY(20);
    name.setFont(new Font("华文正楷", 16));
    Label Session = new Label("Session");
    Session.setLayoutX(35);
    Session.setLayoutY(50);
    Session.setFont(new Font("华文正楷", 16));
    Label win = new Label("Win time");
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
    ListView<String> playerss = new ListView<>();
    playerss.setItems(FXCollections.observableArrayList(players));
    NoticeListItemChangeListener me = new NoticeListItemChangeListener();
    me.myself = s;
    me.myName = username;
    Stage list = new Stage();
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
    Button button = new Button("Register");
    buttonInitial(button, 70, 100, 150, 50);
    button.setBackground(Background.EMPTY);
    Label nameLabel = new Label("user name");
    PasswordField passwordTextField = new PasswordField();
    TextField nameField = new TextField();
    nameField.setPromptText("Input your user name");
    passwordTextField.setPromptText("Input your password");
    nameLabel.setLayoutX(20);
    nameLabel.setLayoutY(30);
    Label passwordLabel = new Label("password");
    passwordLabel.setLayoutX(20);
    passwordLabel.setLayoutY(70);
    nameField.setLayoutX(90);
    nameField.setLayoutY(30);
    passwordTextField.setLayoutX(90);
    passwordTextField.setLayoutY(70);
    addListener(button);
    Pane r = new Pane();
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

