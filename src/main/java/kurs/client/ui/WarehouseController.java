package kurs.client.ui;

import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.CreateWarehouseRequest;
import kurs.client.domain.dto.response.WarehouseResponse;
import kurs.client.ui.component.BaseController;

public class WarehouseController extends BaseController {

  @FXML private TableView<WhRow> whTable;
  @FXML private TableColumn<WhRow, String> colWhName;
  @FXML private TableColumn<WhRow, String> colWhPhone;
  @FXML private TableColumn<WhRow, String> colWhActive;
  @FXML private TextField whNameField;
  @FXML private TextField whPhoneField;
  @FXML private Label whFormError;

  private final ObservableList<WhRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colWhName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colWhPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    colWhActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    whTable.setItems(items);
    handleLoad();
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
