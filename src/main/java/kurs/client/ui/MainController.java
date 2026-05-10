package kurs.client.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import kurs.client.ui.component.BaseController;

public class MainController extends BaseController {

  @FXML private javafx.scene.control.Label userLabel;
  @FXML private StackPane contentPane;

  @FXML private Button navSales;
  @FXML private Button navStock;
  @FXML private Button navProducts;
  @FXML private Button navEmployees;
  @FXML private Button navStores;
  @FXML private Button navWarehouses;
  @FXML private Button navReports;
  @FXML private Button navUsers;

  private Button activeNav;
  private final Map<Button, String> navMap = new LinkedHashMap<>();

  @FXML
  public void initialize() {
    userLabel.setText(session.getLogin() + "  ·  " + session.getRole());

    navMap.put(navSales, "sale.fxml");
    navMap.put(navStock, "stock.fxml");
    navMap.put(navProducts, "product.fxml");
    navMap.put(navEmployees, "employee.fxml");
    navMap.put(navStores, "store.fxml");
    navMap.put(navWarehouses, "warehouse.fxml");
    navMap.put(navReports, "report.fxml");
    navMap.put(navUsers, "user.fxml");

    navUsers.setVisible(session.isAdmin());
    navUsers.setManaged(session.isAdmin());
    navReports.setVisible(session.isAdmin() || session.isAccountant());
    navReports.setManaged(session.isAdmin() || session.isAccountant());
    navEmployees.setVisible(session.isAdmin() || session.isManager());
    navEmployees.setManaged(session.isAdmin() || session.isManager());
    navStores.setVisible(session.isAdmin() || session.isManager());
    navStores.setManaged(session.isAdmin() || session.isManager());
    navWarehouses.setVisible(session.isAdmin() || session.isManager() || session.isAccountant());
    navWarehouses.setManaged(session.isAdmin() || session.isManager() || session.isAccountant());
    navSales.setVisible(!session.isGuest());
    navSales.setManaged(!session.isGuest());

    navProducts.setVisible(true);
    navProducts.setManaged(true);

    navStock.setVisible(true);
    navStock.setManaged(true);

    // Открываем первый доступный раздел
    openFirstTab();
  }

  @FXML
  private void navigate(javafx.event.ActionEvent e) {
    switchTo((Button) e.getSource());
  }

  private void openFirstTab() {
    for (Button button : navMap.keySet()) {
      if (switchTo(button)) {
        return;
      }
    }
  }

  private boolean switchTo(Button nav) {
    if (!nav.isVisible()) return false;

    // Убираем активный стиль с предыдущей кнопки
    if (activeNav != null) {
      activeNav.getStyleClass().remove("nav-item-active");
      activeNav.getStyleClass().add("nav-item");
    }

    nav.getStyleClass().remove("nav-item");
    nav.getStyleClass().add("nav-item-active");
    activeNav = nav;

    // Загружаем панель
    String fxml = navMap.get(nav);
    if (fxml == null) return false;

    try {
      LoadResult<?> result = loadFxml(fxml);
      contentPane.getChildren().setAll(result.root());

      if (result.controller() instanceof NeedsMainController c) {
        c.setMainController(this);
      }
    } catch (Exception ex) {
      showError("Не удалось загрузить раздел: " + ex.getMessage());
      return false;
    }
    return true;
  }

  @FXML
  private void handleLogout() {
    try {
      api.logout();
    } catch (Exception ignored) {
    }
    session.close();
    // Открываем логин
    try {
      LoadResult<?> result = loadFxml("login.fxml");
      if (result.root() instanceof javafx.scene.Parent parent) {
        Stage stage = (Stage) contentPane.getScene().getWindow();
        Scene scene = new Scene(parent, 420, 520);
        scene.getStylesheets().add(CSS);
        stage.setScene(scene);
        stage.setTitle("Авторизация");
        stage.setResizable(false);
        stage.setWidth(420);
        stage.setHeight(520);
      }
    } catch (Exception e) {
      showError("Ошибка при выходе: " + e.getMessage());
    }
  }

  /** Маркерный интерфейс для контроллеров которым нужна ссылка на MainController. */
  public interface NeedsMainController {
    void setMainController(MainController mc);
  }
}
