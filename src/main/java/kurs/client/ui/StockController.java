package kurs.client.ui;

import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.SetStockRequest;
import kurs.client.domain.dto.response.StockResponse;
import kurs.client.ui.component.BaseController;

public class StockController extends BaseController {

  @FXML private TextField locationField;
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

  private final ObservableList<StockRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colLocName.setCellValueFactory(new PropertyValueFactory<>("locationName"));
    colProdName.setCellValueFactory(new PropertyValueFactory<>("productName"));
    colArticle.setCellValueFactory(new PropertyValueFactory<>("article"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
    colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    colUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
    stockTable.setItems(items);
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
  private void handleLoadByLocation() {
    String sid = locationField.getText().trim();
    if (sid.isEmpty()) {
      showError("Укажите ID хранилища");
      return;
    }
    async(
        () -> {
          List<StockResponse> data = api.getStockByLocation(UUID.fromString(sid));
          javafx.application.Platform.runLater(
              () -> items.setAll(data.stream().map(StockRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleSetStock() {
    String locId = setLocField.getText().trim();
    String prodId = setProductField.getText().trim();
    String qtyS = setQtyField.getText().trim();
    if (locId.isEmpty() || prodId.isEmpty() || qtyS.isEmpty()) {
      setError(stockStatusLabel, "Заполните все поля");
      return;
    }
    try {
      int qty = Integer.parseInt(qtyS);
      async(
          () ->
              api.setStock(
                  new SetStockRequest(UUID.fromString(locId), UUID.fromString(prodId), qty)),
          () -> {
            setError(stockStatusLabel, "");
            handleLoadAll();
          },
          msg -> setError(stockStatusLabel, msg));
    } catch (NumberFormatException e) {
      setError(stockStatusLabel, "Некорректное количество");
    }
  }

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
