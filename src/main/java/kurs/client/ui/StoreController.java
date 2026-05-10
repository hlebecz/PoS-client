package kurs.client.ui;

import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.CreateStoreRequest;
import kurs.client.domain.dto.request.UpdateStoreRequest;
import kurs.client.domain.dto.response.*;
import kurs.client.permission.PermissionAction;
import kurs.client.permission.ViewName;
import kurs.client.ui.component.BaseController;
import kurs.client.ui.component.EntityPickerDialog;

public class StoreController extends BaseController {

  @FXML private TableView<StoreRow> storeTable;
  @FXML private TableColumn<StoreRow, String> colStoreName;
  @FXML private TableColumn<StoreRow, String> colStorePhone;
  @FXML private TableColumn<StoreRow, String> colStoreManager;
  @FXML private TableColumn<StoreRow, String> colStoreWh;
  @FXML private TableColumn<StoreRow, String> colStoreActive;

  @FXML private TabPane tabPane;
  @FXML private Tab listTab;
  @FXML private Tab createTab;
  @FXML private Tab updateTab;
  @FXML private TextField storeNameField;
  @FXML private TextField storePhoneField;
  @FXML private TextField storeManagerField;
  @FXML private TextField storeWhField;
  @FXML private Label storeFormError;

  @FXML private TextField updateIdField;
  @FXML private TextField updateNameField;
  @FXML private TextField updatePhoneField;
  @FXML private TextField updateManagerField;
  @FXML private TextField updateWhField;
  @FXML private Label updateFormError;

  @FXML private Button editButton;
  @FXML private Button deactivateButton;
  @FXML private Button activateButton;

  private UUID selectedManagerId = null;
  private UUID selectedWarehouseId = null;
  private UUID updateManagerId = null;
  private UUID updateWarehouseId = null;

  private final ObservableList<StoreRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colStoreName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colStorePhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    colStoreManager.setCellValueFactory(new PropertyValueFactory<>("managerLogin"));
    colStoreWh.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
    colStoreActive.setCellValueFactory(new PropertyValueFactory<>("active"));

    storeTable.setItems(items);

    // Apply permissions - Manager can only update, not create
    hideTabIfNoPermission(createTab, ViewName.STORES, PermissionAction.CREATE);
    hideTabIfNoPermission(updateTab, ViewName.STORES, PermissionAction.UPDATE);
    hideIfNoPermission(editButton, ViewName.STORES, PermissionAction.UPDATE);
    hideIfNoPermission(deactivateButton, ViewName.STORES, PermissionAction.UPDATE);
    hideIfNoPermission(activateButton, ViewName.STORES, PermissionAction.UPDATE);

    handleLoad();
  }

  private void populateUpdateForm(StoreRow row) {
    updateIdField.setText(row.getId());
    updateNameField.setText(row.getName());
    updatePhoneField.setText(row.getPhone());
    updateManagerField.setText(row.getManagerLogin());
    updateWhField.setText(row.getWarehouseName());
    updateManagerId = row.getManagerId() != null ? UUID.fromString(row.getManagerId()) : null;
    updateWarehouseId = row.getWarehouseId() != null ? UUID.fromString(row.getWarehouseId()) : null;

    if (tabPane != null) {
      tabPane.getSelectionModel().select(2);
    }
  }

  @FXML
  private void handleLoad() {
    async(
        () -> {
          List<StoreResponse> data = api.getStores();
          Platform.runLater(() -> items.setAll(data.stream().map(StoreRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleDeactivate() {
    StoreRow sel = storeTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите магазин");
      return;
    }
    async(
        () -> api.deactivateStore(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Магазин деактивирован");
          handleLoad();
        });
  }

  @FXML
  private void handleActivate() {
    StoreRow sel = storeTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите магазин");
      return;
    }
    async(
        () -> api.activateStore(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Магазин активирован");
          handleLoad();
        });
  }

  @FXML
  private void pickManager() {
    async(
        () -> {
          List<UserResponse> users = api.getUsers();
          Platform.runLater(
              () -> {
                EntityPickerDialog<UserResponse> picker =
                    new EntityPickerDialog<>(
                        getStage(storeNameField),
                        "Выберите менеджера",
                        List.of(
                            new EntityPickerDialog.Col<>("Логин", "login"),
                            new EntityPickerDialog.Col<>("Роль", "role")),
                        users,
                        u -> UUID.fromString(u.getId().toString()));
                UUID id = picker.showAndWait();
                if (id != null) {
                  selectedManagerId = id;
                  users.stream()
                      .filter(u -> u.getId().equals(id))
                      .findFirst()
                      .ifPresent(u -> storeManagerField.setText(u.getLogin()));
                }
              });
        },
        null);
  }

  @FXML
  private void pickWarehouse() {
    async(
        () -> {
          List<WarehouseResponse> warehouses = api.getWarehouses();
          Platform.runLater(
              () -> {
                EntityPickerDialog<WarehouseResponse> picker =
                    new EntityPickerDialog<>(
                        getStage(storeNameField),
                        "Выберите склад",
                        List.of(
                            new EntityPickerDialog.Col<>("Название", "name"),
                            new EntityPickerDialog.Col<>("Телефон", "phone")),
                        warehouses,
                        w -> UUID.fromString(w.getId().toString()));
                UUID id = picker.showAndWait();
                if (id != null) {
                  selectedWarehouseId = id;
                  warehouses.stream()
                      .filter(w -> w.getId().equals(id))
                      .findFirst()
                      .ifPresent(w -> storeWhField.setText(w.getName()));
                }
              });
        },
        null);
  }

  @FXML
  private void handleCreate() {
    setError(storeFormError, "");
    String name = storeNameField.getText().trim();
    String phone = storePhoneField.getText().trim();
    if (name.isEmpty() || phone.isEmpty()) {
      setError(storeFormError, "Название и телефон обязательны");
      return;
    }
    CreateStoreRequest req =
        CreateStoreRequest.builder()
            .name(name)
            .phone(phone)
            .managerId(selectedManagerId)
            .warehouseId(selectedWarehouseId)
            .build();
    async(
        () -> api.createStore(req),
        () -> {
          showSuccess("Магазин создан");
          storeNameField.clear();
          storePhoneField.clear();
          storeManagerField.clear();
          storeWhField.clear();
          selectedManagerId = null;
          selectedWarehouseId = null;
          handleLoad();
        },
        msg -> setError(storeFormError, msg));
  }

  @FXML
  private void pickUpdateManager() {
    async(
        () -> {
          List<UserResponse> users = api.getUsers();
          Platform.runLater(
              () -> {
                EntityPickerDialog<UserResponse> picker =
                    new EntityPickerDialog<>(
                        getStage(updateNameField),
                        "Выберите менеджера",
                        List.of(
                            new EntityPickerDialog.Col<>("Логин", "login"),
                            new EntityPickerDialog.Col<>("Роль", "role")),
                        users,
                        u -> UUID.fromString(u.getId().toString()));
                UUID id = picker.showAndWait();
                if (id != null) {
                  updateManagerId = id;
                  users.stream()
                      .filter(u -> u.getId().equals(id))
                      .findFirst()
                      .ifPresent(u -> updateManagerField.setText(u.getLogin()));
                }
              });
        },
        null);
  }

  @FXML
  private void pickUpdateWarehouse() {
    async(
        () -> {
          List<WarehouseResponse> warehouses = api.getWarehouses();
          Platform.runLater(
              () -> {
                EntityPickerDialog<WarehouseResponse> picker =
                    new EntityPickerDialog<>(
                        getStage(updateNameField),
                        "Выберите склад",
                        List.of(
                            new EntityPickerDialog.Col<>("Название", "name"),
                            new EntityPickerDialog.Col<>("Телефон", "phone")),
                        warehouses,
                        w -> UUID.fromString(w.getId().toString()));
                UUID id = picker.showAndWait();
                if (id != null) {
                  updateWarehouseId = id;
                  warehouses.stream()
                      .filter(w -> w.getId().equals(id))
                      .findFirst()
                      .ifPresent(w -> updateWhField.setText(w.getName()));
                }
              });
        },
        null);
  }

  @FXML
  private void handleUpdate() {
    setError(updateFormError, "");
    String idStr = updateIdField.getText().trim();
    if (idStr.isEmpty()) {
      setError(updateFormError, "Выберите магазин из списка");
      return;
    }

    UUID id = UUID.fromString(idStr);
    String name = updateNameField.getText().trim();
    String phone = updatePhoneField.getText().trim();

    UpdateStoreRequest req =
        UpdateStoreRequest.builder()
            .id(id)
            .name(name.isEmpty() ? null : name)
            .phone(phone.isEmpty() ? null : phone)
            .managerId(updateManagerId)
            .warehouseId(updateWarehouseId)
            .build();

    async(
        () -> api.updateStore(req),
        () -> {
          showSuccess("Магазин обновлен");
          updateIdField.clear();
          updateNameField.clear();
          updatePhoneField.clear();
          updateManagerField.clear();
          updateWhField.clear();
          updateManagerId = null;
          updateWarehouseId = null;
          handleLoad();
        },
        msg -> setError(updateFormError, msg));
  }

  @FXML
  private void handleEdit() {
    StoreRow sel = storeTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите магазин из списка");
      return;
    }
    populateUpdateForm(sel);
  }

  public static class StoreRow {
    private final String id, name, phone, managerLogin, warehouseName, active;
    private final String managerId, warehouseId;

    public static StoreRow from(StoreResponse s) {
      return new StoreRow(
          str(s.getId()),
          str(s.getName()),
          str(s.getPhone()),
          str(s.getManagerLogin()),
          str(s.getWarehouseName()),
          s.isActive() ? "Да" : "Нет",
          s.getManagerId() != null ? s.getManagerId().toString() : null,
          s.getWarehouseId() != null ? s.getWarehouseId().toString() : null);
    }

    public StoreRow(
        String id,
        String name,
        String phone,
        String managerLogin,
        String warehouseName,
        String active,
        String managerId,
        String warehouseId) {
      this.id = id;
      this.name = name;
      this.phone = phone;
      this.managerLogin = managerLogin;
      this.warehouseName = warehouseName;
      this.active = active;
      this.managerId = managerId;
      this.warehouseId = warehouseId;
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

    public String getManagerLogin() {
      return managerLogin;
    }

    public String getWarehouseName() {
      return warehouseName;
    }

    public String getActive() {
      return active;
    }

    public String getManagerId() {
      return managerId;
    }

    public String getWarehouseId() {
      return warehouseId;
    }
  }
}
