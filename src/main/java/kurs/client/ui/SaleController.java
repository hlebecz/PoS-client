package kurs.client.ui;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import kurs.client.domain.dto.request.CreateSaleRequest;
import kurs.client.domain.dto.response.SaleResponse;
import kurs.client.domain.dto.response.StockResponse;
import kurs.client.domain.dto.response.StoreResponse;
import kurs.client.ui.component.BaseController;
import kurs.client.ui.component.EntityPickerDialog;

public class SaleController extends BaseController {

  // ── Остатки ───────────────────────────────────────────────────────────────
  @FXML private HBox storePickerRow;
  @FXML private TextField stockStoreField;
  @FXML private TableView<StockRow> stockTable;
  @FXML private TableColumn<StockRow, String> colStockName;
  @FXML private TableColumn<StockRow, String> colStockArticle;
  @FXML private TableColumn<StockRow, String> colStockPrice;
  @FXML private TableColumn<StockRow, String> colStockQty;
  @FXML private TextField addQtyField;

  // ── Корзина ───────────────────────────────────────────────────────────────
  @FXML private TableView<CartItem> cartTable;
  @FXML private TableColumn<CartItem, String> colCartName;
  @FXML private TableColumn<CartItem, String> colCartQty;
  @FXML private TableColumn<CartItem, String> colCartPrice;
  @FXML private TableColumn<CartItem, Void> colCartDel;
  @FXML private Label totalLabel;
  @FXML private Label saleStatusLabel;

  // ── История ───────────────────────────────────────────────────────────────
  @FXML private TextField historyStoreField;
  @FXML private TableView<SaleRow> historyTable;
  @FXML private TableColumn<SaleRow, String> colSaleId;
  @FXML private TableColumn<SaleRow, String> colCashier;
  @FXML private TableColumn<SaleRow, String> colTotal;
  @FXML private TableColumn<SaleRow, String> colDate;
  @FXML private TableColumn<SaleRow, String> colReturn;

  private UUID currentStoreId = null; // для остатков
  private UUID historyStoreId = null; // для истории
  private List<StockRow> stockRows = List.of();

  private final ObservableList<StockRow> stockItems = FXCollections.observableArrayList();
  private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
  private final ObservableList<SaleRow> historyItems = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    // Таблица остатков
    colStockName.setCellValueFactory(new PropertyValueFactory<>("productName"));
    colStockArticle.setCellValueFactory(new PropertyValueFactory<>("productArticle"));
    colStockPrice.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
    colStockQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    stockTable.setItems(stockItems);

    // Двойной клик на остатке — добавить в корзину
    stockTable.setOnMouseClicked(
        e -> {
          if (e.getClickCount() == 2) handleAddFromStock();
        });

    // Корзина
    colCartName.setCellValueFactory(new PropertyValueFactory<>("productName"));
    colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantityStr"));
    colCartPrice.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    colCartDel.setCellFactory(
        col ->
            new TableCell<>() {
              private final Button btn = new Button("✕");

              {
                btn.getStyleClass().add("btn-danger");
                btn.setStyle("-fx-padding: 2 7 2 7; -fx-font-size: 11px;");
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

    // Кассир не может выбирать точку — берётся автоматически
    boolean canPickStore = session.isAdmin() || session.isManager();
    storePickerRow.setVisible(canPickStore);
    storePickerRow.setManaged(canPickStore);

    if (!canPickStore) {
      // Для кассира — загружаем остатки его точки автоматически
      loadStockForCashier();
    }

    updateTotal();
  }

  // ── Выбор точки ─────────────────────────────────────────────────────────

  @FXML
  private void pickStockStore() {
    async(
        () -> {
          List<StoreResponse> stores = api.getActiveStores();
          Platform.runLater(
              () -> {
                UUID id =
                    new EntityPickerDialog<>(
                            getStage(stockStoreField),
                            "Выберите торговую точку",
                            List.of(
                                new EntityPickerDialog.Col<>("Название", "name"),
                                new EntityPickerDialog.Col<>("Телефон", "phone")),
                            stores,
                            s -> s.getId())
                        .showAndWait();

                if (id != null) {
                  currentStoreId = id;
                  stores.stream()
                      .filter(s -> s.getId().equals(id))
                      .findFirst()
                      .ifPresent(s -> stockStoreField.setText(s.getName()));
                  loadStock(id);
                }
              });
        },
        null);
  }

  @FXML
  private void pickHistoryStore() {
    async(
        () -> {
          List<StoreResponse> stores = api.getActiveStores();
          Platform.runLater(
              () -> {
                UUID id =
                    new EntityPickerDialog<>(
                            getStage(historyStoreField),
                            "Выберите торговую точку",
                            List.of(
                                new EntityPickerDialog.Col<>("Название", "name"),
                                new EntityPickerDialog.Col<>("Телефон", "phone")),
                            stores,
                            s -> s.getId())
                        .showAndWait();

                if (id != null) {
                  historyStoreId = id;
                  stores.stream()
                      .filter(s -> s.getId().equals(id))
                      .findFirst()
                      .ifPresent(s -> historyStoreField.setText(s.getName()));
                }
              });
        },
        null);
  }

  private void loadStock(UUID storeId) {
    async(
        () -> {
          List<StockResponse> data = api.getStockByLocation(storeId);
          stockRows = data.stream().map(StockRow::from).toList();
          Platform.runLater(() -> stockItems.setAll(stockRows));
        },
        null);
  }

  /** Для кассира — определяем его точку через Employee. */
  private void loadStockForCashier() {
    async(
        () -> {
          // Кассир видит только свою точку — берём из первого employee по userId
          // Сервер отдаст только его точку при getEmployeesByStore
          // Загружаем все остатки — сервер ограничит по роли
          List<StockResponse> data = api.getAllStock();
          stockRows = data.stream().map(StockRow::from).toList();
          Platform.runLater(
              () -> {
                stockItems.setAll(stockRows);
                stockStoreField.setText("Ваша точка");
              });
        },
        null);
  }

  // ── Корзина ──────────────────────────────────────────────────────────────

  @FXML
  private void handleAddFromStock() {
    StockRow sel = stockTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      setError(saleStatusLabel, "Выберите товар");
      return;
    }

    int qty;
    try {
      qty = Integer.parseInt(addQtyField.getText().trim());
      if (qty <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
      setError(saleStatusLabel, "Укажите корректное количество");
      return;
    }

    int available = Integer.parseInt(sel.getQuantity());
    if (qty > available) {
      setError(saleStatusLabel, "Недостаточно товара (доступно: " + available + ")");
      return;
    }

    // Если товар уже в корзине — увеличиваем количество
    cartItems.stream()
        .filter(c -> c.getProductId().equals(sel.getProductId()))
        .findFirst()
        .ifPresentOrElse(
            c -> {
              int newQty = c.getQuantity() + qty;
              if (newQty > available) {
                setError(saleStatusLabel, "Итоговое кол-во превысит остаток");
                return;
              }
              int idx = cartItems.indexOf(c);
              cartItems.set(
                  idx,
                  new CartItem(
                      sel.getProductId(), sel.getProductName(), sel.getProductPrice(), newQty));
            },
            () ->
                cartItems.add(
                    new CartItem(
                        sel.getProductId(), sel.getProductName(), sel.getProductPrice(), qty)));

    addQtyField.setText("1");
    setError(saleStatusLabel, "");
    updateTotal();
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
          Platform.runLater(
              () -> {
                cartItems.clear();
                updateTotal();
                // Обновляем остатки
                if (currentStoreId != null) loadStock(currentStoreId);
                showSuccess("Продажа проведена!\nИтог: " + result.getTotal() + " BYN");
              });
        },
        null,
        msg -> setError(saleStatusLabel, msg));
  }

  private void updateTotal() {
    BigDecimal total =
        cartItems.stream()
            .map(
                c ->
                    new BigDecimal(c.getProductPrice())
                        .multiply(BigDecimal.valueOf(c.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    totalLabel.setText(total.setScale(2, java.math.RoundingMode.HALF_UP) + " BYN");
  }


  @FXML
  private void handleLoadHistory() {
    if (historyStoreId == null) {
      showError("Выберите магазин");
      return;
    }
    async(
        () -> {
          List<SaleResponse> sales = api.getSalesByStore(historyStoreId);
          Platform.runLater(() -> historyItems.setAll(sales.stream().map(SaleRow::from).toList()));
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

  // ── Row models ────────────────────────────────────────────────────────────

  public static class StockRow {
    private final String productId, productName, productArticle, productPrice, quantity;

    public static StockRow from(StockResponse s) {
      return new StockRow(
          str(s.getProductId()),
          str(s.getProductName()),
          str(s.getProductArticle()),
          str(s.getProductPrice()),
          str(s.getQuantity()));
    }

    public StockRow(String pid, String pn, String pa, String pp, String q) {
      productId = pid;
      productName = pn;
      productArticle = pa;
      productPrice = pp;
      quantity = q;
    }

    public String getProductId() {
      return productId;
    }

    public String getProductName() {
      return productName;
    }

    public String getProductArticle() {
      return productArticle;
    }

    public String getProductPrice() {
      return productPrice;
    }

    public String getQuantity() {
      return quantity;
    }

    private static String str(Object o) {
      return o != null ? o.toString() : "—";
    }
  }

  public static class CartItem {
    private final String productId, productName, productPrice;
    private final int quantity;

    public CartItem(String pid, String name, String price, int qty) {
      productId = pid;
      productName = name;
      productPrice = price;
      quantity = qty;
    }

    public String getProductId() {
      return productId;
    }

    public String getProductName() {
      return productName;
    }

    public String getProductPrice() {
      return productPrice;
    }

    public int getQuantity() {
      return quantity;
    }

    public String getQuantityStr() {
      return String.valueOf(quantity);
    }

    public String getSubtotal() {
      try {
        return new BigDecimal(productPrice)
            .multiply(BigDecimal.valueOf(quantity))
            .setScale(2, java.math.RoundingMode.HALF_UP)
            .toString();
      } catch (Exception e) {
        return "—";
      }
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
      return isReturn ? "↩ Да" : "—";
    }

    private static String str(Object o) {
      return o != null ? o.toString() : "—";
    }
  }
}
