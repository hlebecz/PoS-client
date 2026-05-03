package kurs.client.ui;

import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.CreateStoreRequest;
import kurs.client.domain.dto.response.*;
import kurs.client.ui.component.BaseController;
import kurs.client.ui.component.EntityPickerDialog;

public class StoreController extends BaseController {

  @FXML private TableView<StoreRow> storeTable;
  @FXML private TableColumn<StoreRow, String> colStoreName;
  @FXML private TableColumn<StoreRow, String> colStorePhone;
  @FXML private TableColumn<StoreRow, String> colStoreManager;
  @FXML private TableColumn<StoreRow, String> colStoreWh;
  @FXML private TableColumn<StoreRow, String> colStoreActive;

  @FXML private TextField storeNameField;
  @FXML private TextField storePhoneField;
  @FXML private TextField storeManagerField; // отображает login менеджера
  @FXML private TextField storeWhField; // отображает название склада
  @FXML private Label storeFormError;

  private UUID selectedManagerId = null;
  private UUID selectedWarehouseId = null;

  private final ObservableList<StoreRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colStoreName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colStorePhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    colStoreManager.setCellValueFactory(new PropertyValueFactory<>("managerLogin"));
    colStoreWh.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
    colStoreActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    storeTable.setItems(items);
    handleLoad();
  }

  // ── List ─────────────────────────────────────────────────────────────────

  @FXML
  private void handleLoad() {
    async(
        () -> {
          List<StoreResponse> data = api.getStores();
          javafx.application.Platform.runLater(
              () -> items.setAll(data.stream().map(StoreRow::from).toList()));
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

  // ── Create ────────────────────────────────────────────────────────────────

  /** Открывает диалог выбора менеджера (из списка пользователей с ролью MANAGER). */
  @FXML
  private void pickManager() {
    async(
        () -> {
          List<UserResponse> users = api.getUsers();
          javafx.application.Platform.runLater(
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

  /** Открывает диалог выбора склада. */
  @FXML
  private void pickWarehouse() {
    async(
        () -> {
          List<WarehouseResponse> warehouses = api.getWarehouses();
          javafx.application.Platform.runLater(
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
        new CreateStoreRequest(name, phone, selectedManagerId, selectedWarehouseId, null);
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

  // ── Row model ─────────────────────────────────────────────────────────────

  public static class StoreRow {
    private final String id, name, phone, managerLogin, warehouseName, active;

    public static StoreRow from(StoreResponse s) {
      return new StoreRow(
          str(s.getId()),
          str(s.getName()),
          str(s.getPhone()),
          s.getManagerLogin() != null ? s.getManagerLogin() : "—",
          s.getWarehouseName() != null ? s.getWarehouseName() : "—",
          s.isActive() ? "Да" : "Нет");
    }

    public StoreRow(String id, String n, String ph, String ml, String wn, String a) {
      this.id = id;
      name = n;
      phone = ph;
      managerLogin = ml;
      warehouseName = wn;
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

    public String getManagerLogin() {
      return managerLogin;
    }

    public String getWarehouseName() {
      return warehouseName;
    }

    public String getActive() {
      return active;
    }
  }
}
