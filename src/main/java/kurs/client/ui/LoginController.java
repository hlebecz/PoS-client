package kurs.client.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kurs.client.domain.dto.request.LoginRequest;
import kurs.client.network.ServerClient;
import kurs.client.ui.component.BaseController;

public class LoginController extends BaseController {

  @FXML private TextField hostField;
  @FXML private TextField portField;
  @FXML private TextField loginField;
  @FXML private PasswordField passField;
  @FXML private Button loginBtn;
  @FXML private Label errorLabel;
  @FXML private ProgressIndicator spinner;

  @FXML private VBox loginForm;
  @FXML private VBox registerForm;
  @FXML private TextField registerLoginField;
  @FXML private PasswordField registerPassField;
  @FXML private PasswordField registerConfirmPassField;
  @FXML private Button registerBtn;
  @FXML private Label registerErrorLabel;
  @FXML private ProgressIndicator registerSpinner;

  private static final Gson GSON = new Gson();

  @FXML
  private void handleLogin() {
    errorLabel.setText("");

    String host = hostField.getText().trim();
    String portS = portField.getText().trim();
    String login = loginField.getText().trim();
    String pass = passField.getText();

    if (host.isEmpty() || portS.isEmpty() || login.isEmpty() || pass.isEmpty()) {
      errorLabel.setText("Заполните все поля");
      return;
    }

    int port;
    try {
      port = Integer.parseInt(portS);
    } catch (NumberFormatException e) {
      errorLabel.setText("Некорректный порт");
      return;
    }

    loginBtn.setDisable(true);
    spinner.setVisible(true);

    Thread.ofVirtual()
        .start(
            () -> {
              try {
                ServerClient client = new ServerClient(host, port);
                client.connect();

                session.open(client, "pending", login, "GUEST");

                String token = api.login(new LoginRequest(login, pass));
                String role = decodeRole(token);

                session.open(client, token, login, role);

                Platform.runLater(
                    () -> {
                      new MainWindow().show();
                      ((Stage) loginBtn.getScene().getWindow()).close();
                    });

              } catch (Exception e) {
                session.close();
                Platform.runLater(
                    () -> {
                      errorLabel.setText(
                          e.getMessage() != null ? e.getMessage() : "Не удалось подключиться");
                      loginBtn.setDisable(false);
                      spinner.setVisible(false);
                    });
              }
            });
  }

  /** Декодирует роль из JWT payload (base64url) без верификации подписи. */
  private String decodeRole(String token) {
    try {
      String payload = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]));
      return GSON.fromJson(payload, JsonObject.class).get("role").getAsString();
    } catch (Exception e) {
      return "GUEST";
    }
  }

  @FXML
  private void switchToRegister() {
    loginForm.setVisible(false);
    loginForm.setManaged(false);
    registerForm.setVisible(true);
    registerForm.setManaged(true);

    registerLoginField.clear();
    registerPassField.clear();
    registerConfirmPassField.clear();
    registerErrorLabel.setText("");
    errorLabel.setText("");
  }

  @FXML
  private void switchToLogin() {
    registerForm.setVisible(false);
    registerForm.setManaged(false);
    loginForm.setVisible(true);
    loginForm.setManaged(true);

    registerErrorLabel.setText("");
    errorLabel.setText("");
  }

  @FXML
  private void handleRegister() {
    registerErrorLabel.setText("");

    String host = hostField.getText().trim();
    String portS = portField.getText().trim();
    String login = registerLoginField.getText().trim();
    String pass = registerPassField.getText();
    String confirmPass = registerConfirmPassField.getText();

    if (host.isEmpty() || portS.isEmpty()) {
      registerErrorLabel.setText("Заполните хост и порт");
      return;
    }

    if (login.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
      registerErrorLabel.setText("Заполните все поля");
      return;
    }

    if (pass.length() < 6) {
      registerErrorLabel.setText("Пароль должен содержать минимум 6 символов");
      return;
    }

    if (!pass.equals(confirmPass)) {
      registerErrorLabel.setText("Пароли не совпадают");
      return;
    }

    int port;
    try {
      port = Integer.parseInt(portS);
    } catch (NumberFormatException e) {
      registerErrorLabel.setText("Некорректный порт");
      return;
    }

    registerBtn.setDisable(true);
    registerSpinner.setVisible(true);

    Thread.ofVirtual()
        .start(
            () -> {
              try {
                ServerClient client = new ServerClient(host, port);
                client.connect();

                session.open(client, "pending", login, "GUEST");

                api.register(new LoginRequest(login, pass));

                Platform.runLater(
                    () -> {
                      registerErrorLabel.setStyle("-fx-text-fill: green;");
                      registerErrorLabel.setText("Регистрация успешна! Войдите с вашими данными");

                      Thread.ofVirtual()
                          .start(
                              () -> {
                                try {
                                  Thread.sleep(2000);
                                  Platform.runLater(
                                      () -> {
                                        switchToLogin();
                                        loginField.setText(login);
                                        registerErrorLabel.setStyle("");
                                        registerBtn.setDisable(false);
                                        registerSpinner.setVisible(false);
                                      });
                                } catch (InterruptedException e) {
                                  // Ignore
                                }
                              });
                    });

              } catch (Exception e) {
                session.close();
                Platform.runLater(
                    () -> {
                      registerErrorLabel.setStyle("");
                      registerErrorLabel.setText(
                          e.getMessage() != null
                              ? e.getMessage()
                              : "Не удалось зарегистрироваться");
                      registerBtn.setDisable(false);
                      registerSpinner.setVisible(false);
                    });
              }
            });
  }
}
