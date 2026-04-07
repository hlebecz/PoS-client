package kurs.client;

import javafx.application.Application;
import javafx.stage.Stage;

import kurs.client.session.Session;
import kurs.client.ui.login.LoginWindow;

public class ClientApp extends Application {

  @Override
  public void start(Stage primaryStage) {
    primaryStage.hide();
    new LoginWindow().show();
  }

  @Override
  public void stop() {
    Session.getInstance().close();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
