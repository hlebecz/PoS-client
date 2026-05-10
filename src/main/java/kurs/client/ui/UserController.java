package kurs.client.ui;

import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.CreateUserRequest;
import kurs.client.domain.dto.response.UserResponse;
import kurs.client.domain.entity.UserRole;
import kurs.client.permission.PermissionAction;
import kurs.client.permission.ViewName;
import kurs.client.ui.component.BaseController;

public class UserController extends BaseController {

  @FXML private TableView<UserRow> userTable;
  @FXML private TableColumn<UserRow, String> colLogin;
  @FXML private TableColumn<UserRow, String> colRole;
  @FXML private TableColumn<UserRow, String> colEmployee;
  @FXML private TableColumn<UserRow, String> colActive;
  @FXML private TableColumn<UserRow, String> colCreatedAt;

  @FXML private TabPane tabPane;
  @FXML private Tab listTab;
  @FXML private Tab createTab;
  @FXML private TextField userLoginField;
  @FXML private PasswordField userPassField;
  @FXML private ComboBox<UserRole> userRoleBox;
  @FXML private Label userFormError;

  @FXML private Button deactivateButton;
  @FXML private Button activateButton;

  private final ObservableList<UserRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
    colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
    colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    userTable.setItems(items);
    userRoleBox.setItems(FXCollections.observableArrayList(UserRole.values()));

    // Apply permissions
    hideTabIfNoPermission(createTab, ViewName.USERS, PermissionAction.CREATE);
    hideIfNoPermission(deactivateButton, ViewName.USERS, PermissionAction.UPDATE);
    hideIfNoPermission(activateButton, ViewName.USERS, PermissionAction.UPDATE);

    handleLoad();
  }

  @FXML
  private void handleLoad() {
    async(
        () -> {
          List<UserResponse> data = api.getUsers();
          javafx.application.Platform.runLater(
              () -> items.setAll(data.stream().map(UserRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleDeactivate() {
    UserRow sel = userTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите пользователя");
      return;
    }
    async(
        () -> api.deactivateUser(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Пользователь деактивирован");
          handleLoad();
        });
  }

  @FXML
  private void handleActivate() {
    UserRow sel = userTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите пользователя");
      return;
    }
    async(
        () -> api.activateUser(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Пользователь активирован");
          handleLoad();
        });
  }

  @FXML
  private void handleCreate() {
    setError(userFormError, "");
    if (userRoleBox.getValue() == null) {
      setError(userFormError, "Выберите роль");
      return;
    }
    async(
        () ->
            api.createUser(
                new CreateUserRequest(
                    userLoginField.getText().trim(),
                    userPassField.getText(),
                    userRoleBox.getValue())),
        () -> {
          showSuccess("Пользователь создан");
          userLoginField.clear();
          userPassField.clear();
          userRoleBox.setValue(null);
          handleLoad();
        },
        msg -> setError(userFormError, msg));
  }

  public static class UserRow {
    private final String id, login, role, employeeName, active, createdAt;

    public static UserRow from(UserResponse u) {
      return new UserRow(
          str(u.getId()),
          str(u.getLogin()),
          str(u.getRole()),
          u.getEmployeeName() != null ? u.getEmployeeName() : "—",
          u.isActive() ? "Да" : "Нет",
          str(u.getCreatedAt()));
    }

    public UserRow(String id, String l, String r, String e, String a, String c) {
      this.id = id;
      login = l;
      role = r;
      employeeName = e;
      active = a;
      createdAt = c;
    }

    public String getId() {
      return id;
    }

    public String getLogin() {
      return login;
    }

    public String getRole() {
      return role;
    }

    public String getEmployeeName() {
      return employeeName;
    }

    public String getActive() {
      return active;
    }

    public String getCreatedAt() {
      return createdAt;
    }
  }
}
