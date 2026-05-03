package kurs.client.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

import kurs.client.ui.component.BaseController;

public class MainWindow {

  private Stage stage;

  public void show() {
    stage = new Stage();
    BaseController.LoadResult<?> result = BaseController.loadFxml("main.fxml");
    Scene scene = new Scene(result.root(), 1200, 700);
    scene.getStylesheets().add(BaseController.CSS);
    stage.setScene(scene);
    stage.setTitle("Система управления торговой сетью");
    stage.setMaximized(true);
    stage.show();
  }

  public void show(Stage owner) {
    stage = new Stage();
    stage.initOwner(owner);
    BaseController.LoadResult<?> result = BaseController.loadFxml("main.fxml");
    Scene scene = new Scene(result.root(), 1200, 700);
    scene.getStylesheets().add(BaseController.CSS);
    stage.setScene(scene);
    stage.setTitle("Система управления торговой сетью");
    stage.setMaximized(true);
    stage.show();
  }

  public void close() {
    if (stage != null) {
      stage.close();
    }
  }

  public Stage getStage() {
    return stage;
  }
}
