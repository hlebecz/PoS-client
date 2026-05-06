package kurs.client.ui;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.request.CreateProductRequest;
import kurs.client.domain.dto.request.UpdateProductRequest;
import kurs.client.domain.dto.response.ProductResponse;
import kurs.client.ui.component.BaseController;

public class ProductController extends BaseController {

  @FXML private TableView<ProductRow> productTable;
  @FXML private TableColumn<ProductRow, String> colName;
  @FXML private TableColumn<ProductRow, String> colArticle;
  @FXML private TableColumn<ProductRow, String> colPrice;
  @FXML private TableColumn<ProductRow, String> colCreatedAt;

  @FXML private TabPane tabPane;
  @FXML private TextField searchField;
  @FXML private TextField nameField;
  @FXML private TextField articleField;
  @FXML private TextField priceField;
  @FXML private Label formError;

  @FXML private TextField updateIdField;
  @FXML private TextField updateNameField;
  @FXML private TextField updateArticleField;
  @FXML private TextField updatePriceField;
  @FXML private Label updateFormError;

  private final ObservableList<ProductRow> items = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colArticle.setCellValueFactory(new PropertyValueFactory<>("article"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
    colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

    productTable.setItems(items);
    handleLoad();
  }

  private void populateUpdateForm(ProductRow row) {
    updateIdField.setText(row.getId());
    updateNameField.setText(row.getName());
    updateArticleField.setText(row.getArticle());
    updatePriceField.setText(row.getPrice());
    // Switch to Update tab (index 2)
    if (tabPane != null) {
      tabPane.getSelectionModel().select(2);
    }
  }

  @FXML
  private void handleLoad() {
    async(
        () -> {
          List<ProductResponse> data = api.getProducts();
          Platform.runLater(() -> items.setAll(data.stream().map(ProductRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleSearch() {
    String article = searchField.getText().trim();
    if (article.isEmpty()) {
      showError("Введите артикул для поиска");
      return;
    }
    async(
        () -> {
          ProductResponse product = api.getProductByArticle(article);
          Platform.runLater(() -> items.setAll(List.of(ProductRow.from(product))));
        },
        () -> {},
        msg -> showError("Товар не найден: " + msg));
  }

  @FXML
  private void handleCreate() {
    setError(formError, "");
    String name = nameField.getText().trim();
    String article = articleField.getText().trim();
    String priceStr = priceField.getText().trim();

    if (name.isEmpty()) {
      setError(formError, "Введите название");
      return;
    }
    if (article.isEmpty()) {
      setError(formError, "Введите артикул");
      return;
    }
    if (priceStr.isEmpty()) {
      setError(formError, "Введите цену");
      return;
    }

    BigDecimal price;
    try {
      price = new BigDecimal(priceStr);
      if (price.compareTo(BigDecimal.ZERO) < 0) {
        setError(formError, "Цена не может быть отрицательной");
        return;
      }
    } catch (NumberFormatException e) {
      setError(formError, "Неверный формат цены");
      return;
    }

    CreateProductRequest req =
        CreateProductRequest.builder().name(name).article(article).price(price).build();

    async(
        () -> api.createProduct(req),
        () -> {
          showSuccess("Товар создан");
          nameField.clear();
          articleField.clear();
          priceField.clear();
          handleLoad();
        },
        msg -> setError(formError, msg));
  }

  @FXML
  private void handleUpdate() {
    setError(updateFormError, "");
    String idStr = updateIdField.getText().trim();
    if (idStr.isEmpty()) {
      setError(updateFormError, "Выберите товар из списка");
      return;
    }

    UUID id = UUID.fromString(idStr);
    String name = updateNameField.getText().trim();
    String article = updateArticleField.getText().trim();
    String priceStr = updatePriceField.getText().trim();

    BigDecimal price = null;
    if (!priceStr.isEmpty()) {
      try {
        price = new BigDecimal(priceStr);
        if (price.compareTo(BigDecimal.ZERO) < 0) {
          setError(updateFormError, "Цена не может быть отрицательной");
          return;
        }
      } catch (NumberFormatException e) {
        setError(updateFormError, "Неверный формат цены");
        return;
      }
    }

    UpdateProductRequest req =
        UpdateProductRequest.builder()
            .id(id)
            .name(name.isEmpty() ? null : name)
            .article(article.isEmpty() ? null : article)
            .price(price)
            .build();

    async(
        () -> api.updateProduct(req),
        () -> {
          showSuccess("Товар обновлен");
          updateIdField.clear();
          updateNameField.clear();
          updateArticleField.clear();
          updatePriceField.clear();
          handleLoad();
        },
        msg -> setError(updateFormError, msg));
  }

  @FXML
  private void handleDelete() {
    ProductRow sel = productTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите товар");
      return;
    }

    // Only ADMIN can delete
    if (!session.isAdmin()) {
      showError("Только администратор может удалять товары");
      return;
    }

    async(
        () -> api.deleteProduct(UUID.fromString(sel.getId())),
        () -> {
          showSuccess("Товар удален");
          handleLoad();
        });
  }

  @FXML
  private void handleEdit() {
    ProductRow sel = productTable.getSelectionModel().getSelectedItem();
    if (sel == null) {
      showError("Выберите товар из списка");
      return;
    }
    populateUpdateForm(sel);
  }

  public static class ProductRow {
    private final String id, name, article, price, createdAt;

    public static ProductRow from(ProductResponse p) {
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
      return new ProductRow(
          p.getId().toString(),
          p.getName(),
          p.getArticle(),
          p.getPrice().toString(),
          p.getCreatedAt() != null ? p.getCreatedAt().format(fmt) : "");
    }

    public ProductRow(String id, String name, String article, String price, String createdAt) {
      this.id = id;
      this.name = name;
      this.article = article;
      this.price = price;
      this.createdAt = createdAt;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getArticle() {
      return article;
    }

    public String getPrice() {
      return price;
    }

    public String getCreatedAt() {
      return createdAt;
    }
  }
}
