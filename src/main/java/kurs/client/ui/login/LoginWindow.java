package kurs.client.ui.login;

import javafx.stage.Stage;

import kurs.client.ui.component.BaseController;

public class LoginWindow {
  public void show() {
    BaseController.loadFxml("login.fxml", new Stage(), "Авторизация", 420, 520);
  }
}
