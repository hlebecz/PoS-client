package kurs.client.ui;

import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.CreateWarehouseRequest;
import kurs.client.domain.dto.request.UpdateWarehouseRequest;
import kurs.client.domain.dto.response.WarehouseResponse;
import kurs.client.permission.PermissionAction;
import kurs.client.permission.ViewName;
import kurs.client.ui.component.BaseController;

public class WarehouseController extends BaseController {

  @FXML private TableView<WhRow> whTable;
  @FXML private TableColumn<WhRow, String> colWhName;
  @FXML private TableColumn<WhRow, String> colWhPhone;
  @FXML private TableColumn<WhRow, String> colWhActive;

  @FXML private TabPane tabPane;
  @FXML private Tab listTab;
  @FXML private Tab createTab;
  @FXML private Tab updateTab;
  @FXML private TextField whNameField;
  @FXML private TextField whPhoneField;
  @FXML private Label whFormError;

  @FXML private TextField updateIdField;
  @FXML private TextField updateNameField;
  @FXML private TextField updatePhoneField;
  @FXML private Label updateFormError;

  @FXML private Button editButton;
  @FXML private Button deactivateButton;
  @FXML private Button activateButton;

  private final ObservableList<WhRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colWhName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colWhPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    colWhActive.setCellValueFactory(new PropertyValueFactory<>("active"));

    whTable.setItems(items);

    // Apply permissions
    hideTabIfNoPermission(createTab, ViewName.WAREHOUSES, PermissionAction.CREATE);
    hideTabIfNoPermission(updateTab, ViewName.WAREHOUSES, PermissionAction.UPDATE);
    hideIfNoPermission(editButton, ViewName.WAREHOUSES, PermissionAction.UPDATE);
    hideIfNoPermission(deactivateButton, ViewName.WAREHOUSES, PermissionAction.UPDATE);
    hideIfNoPermission(activateButton, ViewName.WAREHOUSES, PermissionAction.UPDATE);

    handleLoad();
  }

  private void populateUpdateForm(WhRow row) {
    updateIdField.setText(row.getId());
    updateNameField.setText(row.getName());
    updatePhoneField.setText(row.getPhone());
    // Switch to Update tab (index 2)
    if (tabPane != null) {
      tabPane.getSelectionModel().select(2);
    }
  }

  @FXML
  private void handleLoad() {
    async(
        () -> {
          List<WarehouseResponse> data = api.getWarehouses();
          javafx.application.Platform.runLater(
              () -> items.setAll(data.stream().map(WhRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleDeactivate() {
    WhRow sel = whTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите склад");
      return;
    }
    async(
        () -> api.deactivateWarehouse(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Склад деактивирован");
          handleLoad();
        });
  }

  @FXML
  private void handleActivate() {
    WhRow sel = whTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите склад");
      return;
    }
    async(
        () -> api.activateWarehouse(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Склад активирован");
          handleLoad();
        });
  }

  @FXML
  private void handleCreate() {
    setError(whFormError, "");
    String name = whNameField.getText().trim();
    if (name.isEmpty()) {
      setError(whFormError, "Введите название");
      return;
    }
    async(
        () ->
            api.createWarehouse(
                new CreateWarehouseRequest(name, whPhoneField.getText().trim(), null)),
        () -> {
          showSuccess("Склад создан");
          whNameField.clear();
          whPhoneField.clear();
          handleLoad();
        },
        msg -> setError(whFormError, msg));
  }

  @FXML
  private void handleUpdate() {
    setError(updateFormError, "");
    String idStr = updateIdField.getText().trim();
    if (idStr.isEmpty()) {
      setError(updateFormError, "Выберите склад из списка");
      return;
    }

    UUID id = UUID.fromString(idStr);
    String name = updateNameField.getText().trim();
    String phone = updatePhoneField.getText().trim();

    UpdateWarehouseRequest req =
        UpdateWarehouseRequest.builder()
            .id(id)
            .name(name.isEmpty() ? null : name)
            .phone(phone.isEmpty() ? null : phone)
            .build();

    async(
        () -> api.updateWarehouse(req),
        () -> {
          showSuccess("Склад обновлен");
          updateIdField.clear();
          updateNameField.clear();
          updatePhoneField.clear();
          handleLoad();
        },
        msg -> setError(updateFormError, msg));
  }

  @FXML
  private void handleEdit() {
    WhRow sel = whTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите склад из списка");
      return;
    }
    populateUpdateForm(sel);
  }

  public static class WhRow {
    private final String id, name, phone, active;

    public static WhRow from(WarehouseResponse w) {
      return new WhRow(
          str(w.getId()), str(w.getName()), str(w.getPhone()), w.isActive() ? "Да" : "Нет");
    }

    public WhRow(String id, String n, String ph, String a) {
      this.id = id;
      name = n;
      phone = ph;
      active = a;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getPhone() {
      return phone;
    }

    public String getActive() {
      return active;
    }
  }
}
