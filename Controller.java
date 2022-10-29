//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Alert;
//import javafx.scene.layout.Pane;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//import javafx.scene.shape.Line;
//import javafx.scene.shape.Rectangle;
//
//
//import java.net.URL;
//import java.util.ResourceBundle;
//
//public class Controller implements Initializable {
//
//  @FXML
//  private Pane base_square;
//
//  @FXML
//  private Rectangle game_panel;
//
//
//
//
//  @Override
//  public void initialize(URL location, ResourceBundle resources) {
//    game_panel.setOnMouseClicked(event -> {
//      int x = (int) (event.getX() / BOUND);
//      int y = (int) (event.getY() / BOUND);
//      if (refreshBoard(x, y)) {
//        TURN = !TURN;
//      }
//    });
//  }
//
//
//
//}
