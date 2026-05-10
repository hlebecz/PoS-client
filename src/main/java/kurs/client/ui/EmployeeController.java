package kurs.client.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.*;
import kurs.client.domain.dto.response.*;
import kurs.client.permission.PermissionAction;
import kurs.client.permission.ViewName;
import kurs.client.ui.component.BaseController;
import kurs.client.ui.component.EntityPickerDialog;

public class EmployeeController extends BaseController {

  @FXML private TableView<EmpRow> empTable;
  @FXML private TableColumn<EmpRow, String> colEmpName;
  @FXML private TableColumn<EmpRow, String> colEmpPos;
  @FXML private TableColumn<EmpRow, String> colEmpStore;
  @FXML private TableColumn<EmpRow, String> colEmpUser;
  @FXML private TableColumn<EmpRow, String> colEmpRate;
  @FXML private TableColumn<EmpRow, String> colEmpHired;
  @FXML private TableColumn<EmpRow, String> colEmpFired;

  @FXML private TabPane tabPane;
  @FXML private Tab listTab;
  @FXML private Tab createTab;
  @FXML private TextField empStoreField;
  @FXML private TextField empUserField;
  @FXML private TextField empName;
  @FXML private TextField empPosition;
  @FXML private TextField empRate;
  @FXML private TextField empPhone;
  @FXML private TextField empEmail;
  @FXML private DatePicker empHiredAt;
  @FXML private Label empFormError;

  @FXML private Button fireButton;

  private UUID selectedStoreId = null;
  private UUID selectedUserId = null;

  private final ObservableList<EmpRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colEmpName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
    colEmpPos.setCellValueFactory(new PropertyValueFactory<>("position"));
    colEmpStore.setCellValueFactory(new PropertyValueFactory<>("storeName"));
    colEmpUser.setCellValueFactory(new PropertyValueFactory<>("userLogin"));
    colEmpRate.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
    colEmpHired.setCellValueFactory(new PropertyValueFactory<>("hiredAt"));
    colEmpFired.setCellValueFactory(new PropertyValueFactory<>("firedAt"));
    empTable.setItems(items);

    // Apply permissions
    hideTabIfNoPermission(createTab, ViewName.EMPLOYEES, PermissionAction.CREATE);
    hideIfNoPermission(fireButton, ViewName.EMPLOYEES, PermissionAction.UPDATE);

    handleLoad();
  }

  @FXML
  private void handleLoad() {
    async(
        () -> {
          List<EmployeeResponse> data = api.getEmployees();
          javafx.application.Platform.runLater(
              () -> items.setAll(data.stream().map(EmpRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleFire() {
    EmpRow sel = empTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите сотрудника");
      return;
    }
    async(
        () ->
            api.fireEmployee(
                new FireEmployeeRequest(UUID.fromString(sel.getId()), LocalDate.now())),
        () -> {
          showSuccess("Сотрудник уволен");
          handleLoad();
        });
  }

  @FXML
  private void pickStore() {
    async(
        () -> {
          List<StoreResponse> stores = api.getActiveStores();
          javafx.application.Platform.runLater(
              () -> {
                EntityPickerDialog<StoreResponse> picker =
                    new EntityPickerDialog<>(
                        getStage(empName),
                        "Выберите магазин",
                        List.of(
                            new EntityPickerDialog.Col<>("Название", "name"),
                            new EntityPickerDialog.Col<>("Телефон", "phone")),
                        stores,
                        s -> UUID.fromString(s.getId().toString()));
                UUID id = picker.showAndWait();
                if (id != null) {
                  selectedStoreId = id;
                  stores.stream()
                      .filter(s -> s.getId().equals(id))
                      .findFirst()
                      .ifPresent(s -> empStoreField.setText(s.getName()));
                }
              });
        },
        null);
  }

  @FXML
  private void pickUser() {
    async(
        () -> {
          List<UserResponse> users = api.getUsers();
          javafx.application.Platform.runLater(
              () -> {
                EntityPickerDialog<UserResponse> picker =
                    new EntityPickerDialog<>(
                        getStage(empName),
                        "Выберите учётную запись",
                        List.of(
                            new EntityPickerDialog.Col<>("Логин", "login"),
                            new EntityPickerDialog.Col<>("Роль", "role")),
                        users,
                        u -> UUID.fromString(u.getId().toString()));
                UUID id = picker.showAndWait();
                if (id != null) {
                  selectedUserId = id;
                  users.stream()
                      .filter(u -> u.getId().equals(id))
                      .findFirst()
                      .ifPresent(u -> empUserField.setText(u.getLogin()));
                }
              });
        },
        null);
  }

  @FXML
  private void handleCreate() {
    setError(empFormError, "");
    if (selectedStoreId == null) {
      setError(empFormError, "Выберите магазин");
      return;
    }
    if (empName.getText().isBlank()) {
      setError(empFormError, "Введите ФИО");
      return;
    }
    try {
      CreateEmployeeRequest req =
          new CreateEmployeeRequest(
              selectedStoreId,
              selectedUserId,
              null,
              empName.getText().trim(),
              empPosition.getText().trim(),
              new BigDecimal(empRate.getText().trim()),
              empPhone.getText().trim(),
              empEmail.getText().trim(),
              empHiredAt.getValue());
      async(
          () -> api.createEmployee(req),
          () -> {
            showSuccess("Сотрудник создан");
            clearForm();
            handleLoad();
          },
          msg -> setError(empFormError, msg));
    } catch (NumberFormatException e) {
      setError(empFormError, "Некорректная ставка");
    }
  }

  private void clearForm() {
    empStoreField.clear();
    empUserField.clear();
    empName.clear();
    empPosition.clear();
    empRate.clear();
    empPhone.clear();
    empEmail.clear();
    empHiredAt.setValue(null);
    selectedStoreId = null;
    selectedUserId = null;
  }

  public static class EmpRow {
    private final String id, fullName, position, storeName, userLogin, hourlyRate, hiredAt, firedAt;

    public static EmpRow from(EmployeeResponse e) {
      return new EmpRow(
          str(e.getId()),
          str(e.getFullName()),
          str(e.getPosition()),
          str(e.getStoreName()),
          e.getUserLogin() != null ? e.getUserLogin() : "—",
          str(e.getHourlyRate()),
          str(e.getHiredAt()),
          e.getFiredAt() != null ? e.getFiredAt().toString() : "—");
    }

    public EmpRow(
        String id, String fn, String pos, String sn, String ul, String hr, String ha, String fa) {
      this.id = id;
      fullName = fn;
      position = pos;
      storeName = sn;
      userLogin = ul;
      hourlyRate = hr;
      hiredAt = ha;
      firedAt = fa;
    }

    public String getId() {
      return id;
    }

    public String getFullName() {
      return fullName;
    }

    public String getPosition() {
      return position;
    }

    public String getStoreName() {
      return storeName;
    }

    public String getUserLogin() {
      return userLogin;
    }

    public String getHourlyRate() {
      return hourlyRate;
    }

    public String getHiredAt() {
      return hiredAt;
    }

    public String getFiredAt() {
      return firedAt;
    }
  }
}
