package kurs.client;

import javafx.application.Application;
import javafx.stage.Stage;

import kurs.client.session.Session;
import kurs.client.ui.LoginWindow;

public class ClientApp extends Application {

  @Override
  public void start(Stage primaryStage) {
    new LoginWindow().show(primaryStage);
  }

  @Override
  public void stop() {
    Session session = Session.getInstance();
    if (session.isOpen()) {
      session.close();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
