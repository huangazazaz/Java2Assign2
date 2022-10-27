import javafx.application.Application;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.swing.ImageIconUIResource;

import javax.swing.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Login extends Application {
  BackgroundFill backgroundFill = new BackgroundFill(Paint.valueOf("#BBBB00"), new CornerRadii(20), Insets.EMPTY);
  Background background = new Background(backgroundFill);

  Font textFont = new Font(Font.getDefault().getName(), 20);
  Font buttonFont = new Font(Font.getDefault().getName(), 20);
  Font enterFont = new Font("华文新魏", 23);

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Login");
    primaryStage.getIcons().add(new Image("file:/D:/idea/assign2/image/sample.png"));
//    primaryStage.initStyle(StageStyle.UNDECORATED);
    primaryStage.setWidth(500);
    primaryStage.setHeight(500);
    primaryStage.setResizable(false);

    Pane pane = new Pane();
    pane.setBackground(new Background(new BackgroundImage(new Image("file:/D:/idea/assign2/image/sample.png"),
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.CENTER,
        new BackgroundSize(500, 500, false, false, false, false))));


    Button register = new Button("Register");
    Button login = new Button("Login");
    Button forgot = new Button("Forgot");
    Button tourist = new Button("Tourist");
    buttonInitial(register, 65, 250, 150, 75);
    buttonInitial(login, 270, 250, 150, 75);
    buttonInitial(forgot, 65, 345, 150, 75);
    buttonInitial(tourist, 270, 345, 150, 75);
    addListener(register);
    addListener(login);
    addListener(forgot);
    addListener(tourist);

    tourist.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        try {
          primaryStage.close();
          System.out.println("handle");
          lobby(new Socket(InetAddress.getLocalHost(), 2000));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        System.out.println(event.getCode().getName());
        if (event.getCode().getName().equals("Enter")) {
          try {
            primaryStage.close();
            System.out.println("handle");
            lobby(new Socket(InetAddress.getLocalHost(), 2000));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    });

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


    pane.getChildren().addAll(register, usernameField, passwordField, login, forgot, tourist, username, password);
    primaryStage.setScene(new Scene(pane));
    primaryStage.show();
  }

  void lobby(Socket s) {
    Stage lobby = new Stage();
    lobby.setResizable(false);
    lobby.setWidth(300);
    lobby.setHeight(300);

    Label name = new Label("Name");
    Label Session = new Label("Session");
    Label win = new Label("Win times");

    name.setLayoutX(35);
    name.setLayoutY(50);
    name.setFont(new Font("华文正楷", 16));
    Session.setLayoutX(100);
    Session.setLayoutY(50);
    Session.setFont(new Font("华文正楷", 16));
    win.setLayoutX(180);
    win.setLayoutY(50);
    win.setFont(new Font("华文正楷", 16));

    Button list = new Button("Online\nPlayers");
    buttonInitial(list, 40, 130, 200, 80);
    list.setOnAction(new EventHandler() {
      @Override
      public void handle(Event event) {

        Message request = new Message(null, Message.Type.PLAYER, s.getInetAddress().getHostName(), null);
        SocketUtil.send(s, request);
        System.out.println(s.getInetAddress());
        Object receive = SocketUtil.receive(s);
        if (receive instanceof Message) {
          Message response = (Message) receive;
          playerList((List<String>) response.getContent());
        }
      }
    });

    Pane account = new Pane();
    account.getChildren().addAll(name, Session, win, list);

    Scene scene = new Scene(account);
    lobby.setScene(scene);
    lobby.show();
  }

  void playerList(List<String> players) {
    Stage list = new Stage();
    ListView<String> playerss = new ListView<>();
    playerss.setItems(FXCollections.observableArrayList(players));
    playerss.getSelectionModel().selectedItemProperty().addListener(new NoticeListItemChangeListener());
    ScrollPane player = new ScrollPane();
    player.setContent(playerss);
    Scene scene = new Scene(player);
    list.setScene(scene);
    list.show();
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
    button.setOnMouseExited(new EventHandler() {

      @Override
      public void handle(Event event) {
        button.setFont(buttonFont);
      }
    });
  }
}

class NoticeListItemChangeListener implements ChangeListener<Object> {

  @Override
  public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
    //这里写自己的代码
    System.out.println(newValue);
  }

}
