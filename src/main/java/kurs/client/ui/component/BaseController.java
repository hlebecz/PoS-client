package kurs.client.ui.component;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import kurs.client.network.ApiClient;
import kurs.client.session.Session;

public abstract class BaseController {

  protected final ApiClient api = new ApiClient();
  protected final Session session = Session.getInstance();

  public static final String CSS =
      BaseController.class.getResource("/css/app.css").toExternalForm();

  public record LoadResult<C>(Parent root, C controller) {}

  public static <C> LoadResult<C> loadFxml(String fxmlName) {
    try {
      FXMLLoader loader = new FXMLLoader(BaseController.class.getResource("/fxml/" + fxmlName));
      Parent root = loader.load();
      return new LoadResult<>(root, loader.getController());
    } catch (Exception e) {
      throw new RuntimeException("Не удалось загрузить FXML: " + fxmlName, e);
    }
  }

  protected Stage getStage(Node node) {
    return (Stage) node.getScene().getWindow();
  }

  protected void showError(String message) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle("Ошибка");
    a.setHeaderText(null);
    a.setContentText(message);
    a.showAndWait();
  }

  protected void showSuccess(String message) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle("Успешно");
    a.setHeaderText(null);
    a.setContentText(message);
    a.showAndWait();
  }

  protected void async(Runnable task, Runnable onSuccess) {
    async(task, onSuccess, this::showError);
  }

  protected void async(
      Runnable task, Runnable onSuccess, java.util.function.Consumer<String> onError) {
    Thread.ofVirtual()
        .start(
            () -> {
              try {
                task.run();
                if (onSuccess != null) Platform.runLater(onSuccess);
              } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка";
                if (onError != null) Platform.runLater(() -> onError.accept(msg));
              }
            });
  }

  protected void setError(Label label, String text) {
    if (label != null) Platform.runLater(() -> label.setText(text != null ? text : ""));
  }

  protected static String str(Object o) {
    return o != null ? o.toString() : "—";
  }
}
