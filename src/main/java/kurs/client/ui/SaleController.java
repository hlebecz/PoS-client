package kurs.client.ui;

import java.util.List;
import java.util.UUID;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.*;
import kurs.client.domain.dto.response.*;
import kurs.client.ui.component.BaseController;

public class SaleController extends BaseController {

  // Новая продажа
  @FXML private TextField productIdField;
  @FXML private TextField quantityField;
  @FXML private TableView<CartItem> cartTable;
  @FXML private TableColumn<CartItem, String> colProductId;
  @FXML private TableColumn<CartItem, Integer> colQty;
  @FXML private TableColumn<CartItem, Void> colCartDelete;
  @FXML private Label totalLabel;
  @FXML private Label saleStatusLabel;

  // История
  @FXML private TextField storeIdField;
  @FXML private TableView<SaleRow> historyTable;
  @FXML private TableColumn<SaleRow, String> colSaleId;
  @FXML private TableColumn<SaleRow, String> colCashier;
  @FXML private TableColumn<SaleRow, String> colTotal;
  @FXML private TableColumn<SaleRow, String> colDate;
  @FXML private TableColumn<SaleRow, String> colReturn;

  private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
  private final ObservableList<SaleRow> historyItems = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    // Корзина
    colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
    colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    colCartDelete.setCellFactory(
        col ->
            new TableCell<>() {
              private final Button btn = new Button("✕");

              {
                btn.getStyleClass().add("btn-danger");
                btn.setOnAction(
                    e -> {
                      cartItems.remove(getTableRow().getItem());
                      updateTotal();
                    });
              }

              @Override
              protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
              }
            });
    cartTable.setItems(cartItems);

    // История
    colSaleId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colCashier.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
    colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    colDate.setCellValueFactory(new PropertyValueFactory<>("soldAt"));
    colReturn.setCellValueFactory(new PropertyValueFactory<>("returnStr"));
    historyTable.setItems(historyItems);

    updateTotal();
  }

  @FXML
  private void handleAddToCart() {
    String pid = productIdField.getText().trim();
    String qtyS = quantityField.getText().trim();
    if (pid.isEmpty()) {
      setError(saleStatusLabel, "Укажите ID товара");
      return;
    }
    try {
      int qty = Integer.parseInt(qtyS);
      if (qty <= 0) throw new NumberFormatException();
      cartItems.add(new CartItem(pid, qty));
      productIdField.clear();
      quantityField.setText("1");
      updateTotal();
      setError(saleStatusLabel, "");
    } catch (NumberFormatException e) {
      setError(saleStatusLabel, "Некорректное количество");
    }
  }

  @FXML
  private void handleClearCart() {
    cartItems.clear();
    updateTotal();
  }

  @FXML
  private void handleConfirmSale() {
    if (cartItems.isEmpty()) {
      setError(saleStatusLabel, "Корзина пуста");
      return;
    }
    setError(saleStatusLabel, "");

    List<CreateSaleRequest.SaleItemRequest> items =
        cartItems.stream()
            .map(
                c ->
                    new CreateSaleRequest.SaleItemRequest(
                        UUID.fromString(c.getProductId()), c.getQuantity()))
            .toList();

    async(
        () -> {
          SaleResponse result = api.processSale(new CreateSaleRequest(items));
          javafx.application.Platform.runLater(
              () -> {
                cartItems.clear();
                updateTotal();
                showSuccess("Продажа проведена!\nИтог: " + result.getTotal() + " ₽");
              });
        },
        null,
        msg -> setError(saleStatusLabel, msg));
  }

  @FXML
  private void handleLoadHistory() {
    String sid = storeIdField.getText().trim();
    if (sid.isEmpty()) {
      showError("Укажите ID магазина");
      return;
    }
    async(
        () -> {
          List<SaleResponse> sales = api.getSalesByStore(UUID.fromString(sid));
          javafx.application.Platform.runLater(
              () -> historyItems.setAll(sales.stream().map(SaleRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleProcessReturn() {
    SaleRow sel = historyTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите продажу");
      return;
    }
    if (sel.isReturn()) {
      showError("Нельзя сделать возврат на возврат");
      return;
    }
    async(
        () -> api.processReturn(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Возврат оформлен");
          handleLoadHistory();
        });
  }

  private void updateTotal() {
    totalLabel.setText("Позиций: " + cartItems.size());
  }

  // ── Row models ────────────────────────────────────────────────────────────

  public static class CartItem {
    private final String productId;
    private final int quantity;

    public CartItem(String pid, int qty) {
      productId = pid;
      quantity = qty;
    }

    public String getProductId() {
      return productId;
    }

    public int getQuantity() {
      return quantity;
    }
  }

  public static class SaleRow {
    private final String id, cashierName, total, soldAt;
    private final boolean isReturn;

    public static SaleRow from(SaleResponse s) {
      return new SaleRow(
          str(s.getId()),
          str(s.getCashierName()),
          str(s.getTotal()),
          str(s.getSoldAt()),
          s.isReturn());
    }

    public SaleRow(String id, String cn, String t, String sa, boolean ir) {
      this.id = id;
      cashierName = cn;
      total = t;
      soldAt = sa;
      isReturn = ir;
    }

    public String getId() {
      return id;
    }

    public String getCashierName() {
      return cashierName;
    }

    public String getTotal() {
      return total;
    }

    public String getSoldAt() {
      return soldAt;
    }

    public boolean isReturn() {
      return isReturn;
    }

    public String getReturnStr() {
      return isReturn ? "Да" : "—";
    }

    private static String str(Object o) {
      return o != null ? o.toString() : "—";
    }
  }
}
