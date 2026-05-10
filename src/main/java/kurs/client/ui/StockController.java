package kurs.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.SetStockRequest;
import kurs.client.domain.dto.response.*;
import kurs.client.permission.PermissionAction;
import kurs.client.permission.ViewName;
import kurs.client.ui.component.BaseController;
import kurs.client.ui.component.EntityPickerDialog;

public class StockController extends BaseController {

  @FXML private TextField filterLocationField;
  @FXML private TextField setLocField;
  @FXML private TextField setProductField;
  @FXML private TextField setQtyField;
  @FXML private Label stockStatusLabel;
  @FXML private TableView<StockRow> stockTable;
  @FXML private TableColumn<StockRow, String> colLocName;
  @FXML private TableColumn<StockRow, String> colProdName;
  @FXML private TableColumn<StockRow, String> colArticle;
  @FXML private TableColumn<StockRow, String> colPrice;
  @FXML private TableColumn<StockRow, String> colQty;
  @FXML private TableColumn<StockRow, String> colUpdated;

  @FXML private TabPane tabPane;
  @FXML private Tab listTab;
  @FXML private Tab updateTab;

  private final ObservableList<StockRow> items = FXCollections.observableArrayList();

  private UUID filterLocationId = null;
  private UUID setLocationId = null;
  private UUID setProductId = null;

  @FXML
  public void initialize() {
    colLocName.setCellValueFactory(new PropertyValueFactory<>("locationName"));
    colProdName.setCellValueFactory(new PropertyValueFactory<>("productName"));
    colArticle.setCellValueFactory(new PropertyValueFactory<>("article"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
    colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    colUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
    stockTable.setItems(items);

    // Apply permissions - hide update tab if user cannot update stock
    hideTabIfNoPermission(updateTab, ViewName.STOCK, PermissionAction.UPDATE);

    handleLoadAll();
  }

  @FXML
  private void handleLoadAll() {
    async(
        () -> {
          List<StockResponse> data = api.getAllStock();
          javafx.application.Platform.runLater(
              () -> items.setAll(data.stream().map(StockRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handlePickFilterLocation() {
    async(
        () -> {
          List<StorageLocationItem> locations = getStorageLocationItems();

          javafx.application.Platform.runLater(
              () -> {
                EntityPickerDialog<StorageLocationItem> picker =
                    new EntityPickerDialog<>(
                        getStage(filterLocationField),
                        "Выберите хранилище",
                        List.of(
                            new EntityPickerDialog.Col<>("Название", "name"),
                            new EntityPickerDialog.Col<>("Тип", "type")),
                        locations,
                        StorageLocationItem::id);
                UUID selectedId = picker.showAndWait();
                if (selectedId != null) {
                  filterLocationId = selectedId;
                  locations.stream()
                      .filter(loc -> loc.id().equals(selectedId))
                      .findFirst()
                      .ifPresent(loc -> filterLocationField.setText(loc.name()));
                  loadFilteredStock();
                }
              });
        },
        null);
  }

  private List<StorageLocationItem> getStorageLocationItems() {
    List<StorageLocationItem> locations = new ArrayList<>();

    // All roles can see stores
    List<StoreBasicResponse> stores = api.getStoresActiveBasic();
    stores.forEach(s -> locations.add(new StorageLocationItem(s.getId(), s.getName(), "Магазин")));

    // Only non-guest and non-cashier roles can see warehouses
    if (!session.isGuest() && !session.isCashier()) {
      List<WarehouseBasicResponse> warehouses = api.getWarehousesActiveBasic();
      warehouses.forEach(
          w -> locations.add(new StorageLocationItem(w.getId(), w.getName(), "Склад")));
    }

    return locations;
  }

  @FXML
  private void handleClearFilter() {
    filterLocationId = null;
    filterLocationField.clear();
    handleLoadAll();
  }

  private void loadFilteredStock() {
    if (filterLocationId == null) {
      handleLoadAll();
      return;
    }
    async(
        () -> {
          List<StockResponse> data = api.getStockByLocation(filterLocationId);
          javafx.application.Platform.runLater(
              () -> items.setAll(data.stream().map(StockRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handlePickLocation() {
    async(
        () -> {
          List<StorageLocationItem> locations = new ArrayList<>();

          // All roles can see stores
          List<StoreBasicResponse> stores = api.getStoresActiveBasic();
          stores.forEach(
              s -> locations.add(new StorageLocationItem(s.getId(), s.getName(), "Магазин")));

          // Only non-guest and non-cashier roles can see warehouses
          if (!session.isGuest() && !session.isCashier()) {
            List<WarehouseBasicResponse> warehouses = api.getWarehousesActiveBasic();
            warehouses.forEach(
                w -> locations.add(new StorageLocationItem(w.getId(), w.getName(), "Склад")));
          }

          javafx.application.Platform.runLater(
              () -> {
                EntityPickerDialog<StorageLocationItem> picker =
                    new EntityPickerDialog<>(
                        getStage(setLocField),
                        "Выберите хранилище",
                        List.of(
                            new EntityPickerDialog.Col<>("Название", "name"),
                            new EntityPickerDialog.Col<>("Тип", "type")),
                        locations,
                        StorageLocationItem::id);
                UUID selectedId = picker.showAndWait();
                if (selectedId != null) {
                  setLocationId = selectedId;
                  locations.stream()
                      .filter(loc -> loc.id().equals(selectedId))
                      .findFirst()
                      .ifPresent(loc -> setLocField.setText(loc.name()));
                }
              });
        },
        null);
  }

  @FXML
  private void handlePickProduct() {
    async(
        () -> {
          List<ProductResponse> products = api.getProducts();
          javafx.application.Platform.runLater(
              () -> {
                EntityPickerDialog<ProductResponse> picker =
                    new EntityPickerDialog<>(
                        getStage(setProductField),
                        "Выберите товар",
                        List.of(
                            new EntityPickerDialog.Col<>("Название", "name"),
                            new EntityPickerDialog.Col<>("Артикул", "article"),
                            new EntityPickerDialog.Col<>("Цена", "price")),
                        products,
                        ProductResponse::getId);
                UUID selectedId = picker.showAndWait();
                if (selectedId != null) {
                  setProductId = selectedId;
                  products.stream()
                      .filter(p -> p.getId().equals(selectedId))
                      .findFirst()
                      .ifPresent(
                          p -> setProductField.setText(p.getName() + " (" + p.getArticle() + ")"));
                }
              });
        },
        null);
  }

  @FXML
  private void handleSetStock() {
    if (setLocationId == null || setProductId == null) {
      setError(stockStatusLabel, "Выберите хранилище и товар");
      return;
    }
    String qtyS = setQtyField.getText().trim();
    if (qtyS.isEmpty()) {
      setError(stockStatusLabel, "Укажите количество");
      return;
    }
    try {
      int qty = Integer.parseInt(qtyS);
      async(
          () -> api.setStock(new SetStockRequest(setLocationId, setProductId, qty)),
          () -> {
            setError(stockStatusLabel, "");
            showSuccess("Остаток установлен");
            handleLoadAll();
            // Clear form
            setLocationId = null;
            setProductId = null;
            setLocField.clear();
            setProductField.clear();
            setQtyField.clear();
          },
          msg -> setError(stockStatusLabel, msg));
    } catch (NumberFormatException e) {
      setError(stockStatusLabel, "Некорректное количество");
    }
  }

  // Helper record for combining stores and warehouses
  public record StorageLocationItem(UUID id, String name, String type) {}

  public static class StockRow {
    private final String locationName, productName, article, price, quantity, updatedAt;

    public static StockRow from(StockResponse s) {
      return new StockRow(
          str(s.getStorageLocationName()), str(s.getProductName()),
          str(s.getProductArticle()), str(s.getProductPrice()),
          str(s.getQuantity()), str(s.getUpdatedAt()));
    }

    public StockRow(String ln, String pn, String a, String pr, String q, String u) {
      locationName = ln;
      productName = pn;
      article = a;
      price = pr;
      quantity = q;
      updatedAt = u;
    }

    public String getLocationName() {
      return locationName;
    }

    public String getProductName() {
      return productName;
    }

    public String getArticle() {
      return article;
    }

    public String getPrice() {
      return price;
    }

    public String getQuantity() {
      return quantity;
    }

    public String getUpdatedAt() {
      return updatedAt;
    }

    private static String str(Object o) {
      return o != null ? o.toString() : "—";
    }
  }
}
