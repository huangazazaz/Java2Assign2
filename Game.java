import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Game implements Serializable {
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
      s1.setStartX(105);
      s1.setEndX(105);
      s1.setStartY(0);
      s1.setEndY(300);
      s1.setStroke(Color.GRAY);
      Line s2 = new Line();
      s2.setStartX(195);
      s2.setEndX(195);
      s2.setStartY(0);
      s2.setEndY(300);
      s2.setStroke(Color.GRAY);
      Line h1 = new Line();
      h1.setStartX(0);
      h1.setEndX(300);
      h1.setStartY(105);
      h1.setEndY(105);
      h1.setStroke(Color.GRAY);
      Line h2 = new Line();
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
                  ButtonType buttonTypeCancel = new ButtonType("Wait",
                      ButtonBar.ButtonData.CANCEL_CLOSE);
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
          default:
            break;
        }
      }
    }
  }

  void ifWin(String winner, boolean win) throws IOException {
    if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][0]
        && chessBoard[1][0] == chessBoard[2][0]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      alert.show();
      record(winner);
    } else if (chessBoard[0][1] != 0 && chessBoard[0][1] == chessBoard[1][1]
        && chessBoard[1][1] == chessBoard[2][1]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      alert.show();
      record(winner);
    } else if (chessBoard[0][2] != 0 && chessBoard[0][2] == chessBoard[1][2]
        && chessBoard[1][2] == chessBoard[2][2]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      record(winner);
      alert.show();
    } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[0][1]
        && chessBoard[0][1] == chessBoard[0][2]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      record(winner);
      alert.show();
    } else if (chessBoard[1][0] != 0 && chessBoard[1][0] == chessBoard[1][1]
        && chessBoard[1][1] == chessBoard[1][2]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      record(winner);
      alert.show();
    } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[2][1]
        && chessBoard[2][1] == chessBoard[2][2]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      record(winner);
      alert.show();
    } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][1]
        && chessBoard[1][1] == chessBoard[2][2]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      alert.show();
      record(winner);
    } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[1][1]
        && chessBoard[1][1] == chessBoard[0][2]) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("Winner is " + winner);
      alert.setHeaderText(win ? "You win the game" : "You lose the game");
      alert.setTitle("win");
      alert.show();
      record(winner);
    } else if (count == 9) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setContentText("This is a tie!");
      alert.setHeaderText("tie!");
      alert.setTitle("tie");
      alert.show();
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
  }
}

