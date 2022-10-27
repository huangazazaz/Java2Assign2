import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
  private static final int PLAY_1 = 1;
  private static final int PLAY_2 = 2;
  private static final int EMPTY = 0;
  private static final int BOUND = 90;
  private static final int OFFSET = 15;

  @FXML
  private Pane base_square;

  @FXML
  private Rectangle game_panel;

  private static boolean TURN = true;

  private static final int[][] chessBoard = new int[3][3];
  private static final boolean[][] flag = new boolean[3][3];

  private static int count = 0;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    game_panel.setOnMouseClicked(event -> {
      int x = (int) (event.getX() / BOUND);
      int y = (int) (event.getY() / BOUND);
      if (refreshBoard(x, y)) {
        TURN = !TURN;
        ifWin();
      }
    });
  }

  private boolean refreshBoard(int x, int y) {
    if (chessBoard[x][y] == EMPTY) {
      chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
      count++;
      drawChess();
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
  private void ifWin() {
    if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][0] && chessBoard[1][0] == chessBoard[2][0]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[0][0] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    } else if (chessBoard[0][1] != 0 && chessBoard[0][1] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][1]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[0][1] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    } else if (chessBoard[0][2] != 0 && chessBoard[0][2] == chessBoard[1][2] && chessBoard[1][2] == chessBoard[2][2]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[0][2] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[0][1] && chessBoard[0][1] == chessBoard[0][2]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[0][0] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    } else if (chessBoard[1][0] != 0 && chessBoard[1][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[1][2]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[1][0] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[2][1] && chessBoard[2][1] == chessBoard[2][2]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[2][0] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    } else if (chessBoard[0][0] != 0 && chessBoard[0][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][2]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[0][0] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    } else if (chessBoard[2][0] != 0 && chessBoard[2][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[0][2]) {
      JOptionPane.showMessageDialog(null,
          chessBoard[0][2] == 1 ? "winner is player 1" : "winner is player 2",
          "win!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    }else if (count == 9){
      JOptionPane.showMessageDialog(null,
          "this is a draw",
          "draw!!!",
          JOptionPane.INFORMATION_MESSAGE);
      Platform.exit();
    }
  }
}
