package kurs.client.ui.component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Универсальный диалог выбора сущности с автодополнением.
 *
 * <p>Поле поиска фильтрует таблицу по всем видимым колонкам в реальном времени. Поиск работает
 * через рефлексию — читает геттеры по именам свойств колонок, поэтому не требует реализации
 * toString() в DTO.
 *
 * <p>Использование:
 *
 * <pre>
 *   UUID id = new EntityPickerDialog<>(
 *       ownerStage, "Выберите магазин",
 *       List.of(new Col<>("Название", "name"), new Col<>("Телефон", "phone")),
 *       stores,
 *       StoreResponse::id
 *   ).showAndWait();
 * </pre>
 */
public class EntityPickerDialog<T> {

  public record Col<T>(String title, String property) {}

  private final Stage dialog;
  private final List<Col<T>> columns;
  private final ObservableList<T> allItems;
  private final FilteredList<T> filtered;
  private final TableView<T> table;
  private final Function<T, UUID> idExtractor;
  private UUID result = null;

  private static final String CSS =
      EntityPickerDialog.class.getResource("/css/app.css").toExternalForm();

  public EntityPickerDialog(
      Stage owner,
      String title,
      List<Col<T>> columns,
      List<T> items,
      Function<T, UUID> idExtractor) {
    this.columns = columns;
    this.idExtractor = idExtractor;
    this.allItems = FXCollections.observableArrayList(items);
    this.filtered = new FilteredList<>(allItems, p -> true);
    this.table = buildTable(columns);

    dialog = new Stage();
    dialog.initOwner(owner);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.UTILITY);
    dialog.setTitle(title);
    dialog.setResizable(true);
    dialog.setMinWidth(440);
    dialog.setMinHeight(360);

    VBox root = buildRoot(title);
    Scene scene = new Scene(root, 560, 440);
    scene.getStylesheets().add(CSS);
    dialog.setScene(scene);
  }

  public UUID showAndWait() {
    dialog.showAndWait();
    return result;
  }

  private VBox buildRoot(String title) {
    Label titleLbl = new Label(title);
    titleLbl.getStyleClass().add("section-title");

    TextField search = new TextField();
    search.setPromptText("Начните вводить для поиска...");
    search.getStyleClass().add("picker-search");

    search
        .textProperty()
        .addListener(
            (obs, old, val) -> {
              String q = val.trim().toLowerCase();
              if (q.isEmpty()) {
                filtered.setPredicate(item -> true);
                return;
              }
              filtered.setPredicate(
                  item -> {
                    for (Col<T> col : columns) {
                      String cellValue = getPropertyValue(item, col.property());
                      if (cellValue.toLowerCase().contains(q)) return true;
                    }
                    return false;
                  });

              if (!filtered.isEmpty()) {
                table.getSelectionModel().selectFirst();
              }
            });

    search.setOnAction(e -> confirmSelection());

    table.setItems(filtered);
    VBox.setVgrow(table, Priority.ALWAYS);

    table.setOnMouseClicked(
        e -> {
          if (e.getClickCount() == 2) confirmSelection();
        });

    search.setOnKeyPressed(
        e -> {
          switch (e.getCode()) {
            case DOWN -> {
              table.requestFocus();
              if (table.getSelectionModel().getSelectedIndex() < 0)
                table.getSelectionModel().selectFirst();
            }
            case ESCAPE -> dialog.close();
            default -> {}
          }
        });

    Label countLbl = new Label();
    countLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
    filtered.addListener(
        (javafx.collections.ListChangeListener<T>)
            c -> countLbl.setText("Найдено: " + filtered.size()));
    countLbl.setText("Всего: " + allItems.size());

    HBox searchRow = new HBox(8, search, countLbl);
    searchRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(search, Priority.ALWAYS);

    Button selectBtn = new Button("Выбрать");
    selectBtn.getStyleClass().add("btn-primary");
    selectBtn.setDefaultButton(true);
    selectBtn.setOnAction(e -> confirmSelection());

    Button cancelBtn = new Button("Отмена");
    cancelBtn.getStyleClass().add("btn-secondary");
    cancelBtn.setCancelButton(true);
    cancelBtn.setOnAction(e -> dialog.close());

    HBox buttons = new HBox(8, cancelBtn, selectBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    VBox root = new VBox(12, titleLbl, searchRow, table, buttons);
    root.getStyleClass().add("picker-dialog");
    root.setPadding(new Insets(20));

    javafx.application.Platform.runLater(search::requestFocus);

    return root;
  }

  private TableView<T> buildTable(List<Col<T>> columns) {
    TableView<T> tv = new TableView<>();
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    for (Col<T> col : columns) {
      TableColumn<T, String> tc = new TableColumn<>(col.title());

      tc.setCellValueFactory(
          cellData ->
              new SimpleStringProperty(getPropertyValue(cellData.getValue(), col.property())));
      tv.getColumns().add(tc);
    }
    return tv;
  }

  private void confirmSelection() {
    T selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    result = idExtractor.apply(selected);
    dialog.close();
  }

  /**
   * Читает значение свойства через рефлексию. Пробует методы: getXxx(), xxx(), isXxx() — в таком
   * порядке. Работает с Java records (компоненты имеют метод без get-префикса) и с обычными POJO
   * (геттеры с get-префиксом).
   */
  private String getPropertyValue(T item, String property) {
    if (item == null) return "";
    Class<?> cls = item.getClass();
    String cap = Character.toUpperCase(property.charAt(0)) + property.substring(1);

    for (String name : List.of("get" + cap, property, "is" + cap)) {
      try {
        Method m = cls.getMethod(name);
        Object val = m.invoke(item);
        return val != null ? val.toString() : "";
      } catch (Exception ignored) {
      }
    }
    return "";
  }
}
