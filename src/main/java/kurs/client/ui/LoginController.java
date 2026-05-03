package kurs.client.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
}
