package kurs.client.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

import kurs.client.ui.component.BaseController;

public class LoginWindow {

  public void show(Stage stage) {
    BaseController.LoadResult<?> result = BaseController.loadFxml("login.fxml");
    Scene scene = new Scene(result.root(), 420, 520);
    scene.getStylesheets().add(BaseController.CSS);
    stage.setScene(scene);
    stage.setTitle("Авторизация");
    stage.setResizable(false);
    stage.show();
  }

  public void show() {
    Stage stage = new Stage();
    show(stage);
  }
}
