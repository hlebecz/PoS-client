package kurs.client.network;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import kurs.client.domain.dto.report.EmployeeStats;
import kurs.client.domain.dto.report.SalesReportEntry;
import kurs.client.domain.dto.report.StoreEfficiencyReport;
import kurs.client.domain.dto.request.*;
import kurs.client.domain.dto.response.*;
import kurs.client.domain.model.Request;
import kurs.client.domain.model.RequestType;
import kurs.client.domain.model.Response;
import kurs.client.session.Session;

/** Типизированный API-клиент. Сериализует DTO → JSON (payload), десериализует JSON (data) → DTO. */
public class ApiClient {

  private static final Gson GSON =
      new GsonBuilder()
          .registerTypeAdapter(
              java.time.LocalDate.class,
              (JsonDeserializer<java.time.LocalDate>)
                  (j, t, c) -> java.time.LocalDate.parse(j.getAsString()))
          .registerTypeAdapter(
              java.time.LocalDate.class,
              (JsonSerializer<java.time.LocalDate>) (d, t, c) -> new JsonPrimitive(d.toString()))
          .registerTypeAdapter(
              java.time.LocalDateTime.class,
              (JsonDeserializer<java.time.LocalDateTime>)
                  (j, t, c) -> java.time.LocalDateTime.parse(j.getAsString()))
          .registerTypeAdapter(
              java.time.LocalTime.class,
              (JsonDeserializer<java.time.LocalTime>)
                  (j, t, c) -> java.time.LocalTime.parse(j.getAsString()))
          .create();

  private final Session session = Session.getInstance();

  public String login(LoginRequest req) {
    Response resp = session.getClient().send(new Request(RequestType.LOGIN, GSON.toJson(req)));
    return GSON.fromJson(resp.getData(), String.class);
  }

  public void register(LoginRequest req) {
    session.getClient().send(new Request(RequestType.REGISTER, GSON.toJson(req)));
  }

  public void logout() {
    sendAuth(RequestType.LOGOUT, null);
  }

  public List<UserResponse> getUsers() {
    return parseList(sendAuth(RequestType.GET_USERS, null), UserResponse.class);
  }

  public UserResponse createUser(CreateUserRequest req) {
    return parse(sendAuth(RequestType.CREATE_USER, GSON.toJson(req)), UserResponse.class);
  }

  public UserResponse updateUser(UpdateUserRequest req) {
    return parse(sendAuth(RequestType.UPDATE_USER, GSON.toJson(req)), UserResponse.class);
  }

  public void deleteUser(UUID id) {
    sendAuth(RequestType.DELETE_USER, GSON.toJson(id));
  }

  public UserResponse deactivateUser(UUID id) {
    return parse(sendAuth(RequestType.DEACTIVATE_USER, GSON.toJson(id)), UserResponse.class);
  }

  public UserResponse activateUser(UUID id) {
    return parse(sendAuth(RequestType.ACTIVATE_USER, GSON.toJson(id)), UserResponse.class);
  }

  public List<EmployeeResponse> getEmployees() {
    return parseList(sendAuth(RequestType.GET_EMPLOYEES, null), EmployeeResponse.class);
  }

  public List<EmployeeResponse> getEmployeesByStore(UUID storeId) {
    return parseList(
        sendAuth(RequestType.GET_EMPLOYEES_BY_STORE, GSON.toJson(storeId)), EmployeeResponse.class);
  }

  public EmployeeResponse createEmployee(CreateEmployeeRequest req) {
    return parse(sendAuth(RequestType.CREATE_EMPLOYEE, GSON.toJson(req)), EmployeeResponse.class);
  }

  public EmployeeResponse updateEmployee(UpdateEmployeeRequest req) {
    return parse(sendAuth(RequestType.UPDATE_EMPLOYEE, GSON.toJson(req)), EmployeeResponse.class);
  }

  public void deleteEmployee(UUID id) {
    sendAuth(RequestType.DELETE_EMPLOYEE, GSON.toJson(id));
  }

  public EmployeeResponse fireEmployee(FireEmployeeRequest req) {
    return parse(sendAuth(RequestType.FIRE_EMPLOYEE, GSON.toJson(req)), EmployeeResponse.class);
  }

  public List<StoreResponse> getStores() {
    return parseList(sendAuth(RequestType.GET_STORES, null), StoreResponse.class);
  }

  public List<StoreResponse> getActiveStores() {
    return parseList(sendAuth(RequestType.GET_STORES_ACTIVE, null), StoreResponse.class);
  }

  public List<StoreBasicResponse> getStoresActiveBasic() {
    return parseList(sendAuth(RequestType.GET_STORES_ACTIVE_BASIC, null), StoreBasicResponse.class);
  }

  public StoreResponse createStore(CreateStoreRequest req) {
    return parse(sendAuth(RequestType.CREATE_STORE, GSON.toJson(req)), StoreResponse.class);
  }

  public StoreResponse updateStore(UpdateStoreRequest req) {
    return parse(sendAuth(RequestType.UPDATE_STORE, GSON.toJson(req)), StoreResponse.class);
  }

  public void deleteStore(UUID id) {
    sendAuth(RequestType.DELETE_STORE, GSON.toJson(id));
  }

  public StoreResponse deactivateStore(UUID id) {
    return parse(sendAuth(RequestType.DEACTIVATE_STORE, GSON.toJson(id)), StoreResponse.class);
  }

  public StoreResponse activateStore(UUID id) {
    return parse(sendAuth(RequestType.ACTIVATE_STORE, GSON.toJson(id)), StoreResponse.class);
  }

  public List<WarehouseResponse> getWarehouses() {
    return parseList(sendAuth(RequestType.GET_WAREHOUSES, null), WarehouseResponse.class);
  }

  public List<WarehouseBasicResponse> getWarehousesActiveBasic() {
    return parseList(
        sendAuth(RequestType.GET_WAREHOUSES_ACTIVE_BASIC, null), WarehouseBasicResponse.class);
  }

  public WarehouseResponse createWarehouse(CreateWarehouseRequest req) {
    return parse(sendAuth(RequestType.CREATE_WAREHOUSE, GSON.toJson(req)), WarehouseResponse.class);
  }

  public WarehouseResponse updateWarehouse(UpdateWarehouseRequest req) {
    return parse(sendAuth(RequestType.UPDATE_WAREHOUSE, GSON.toJson(req)), WarehouseResponse.class);
  }

  public void deleteWarehouse(UUID id) {
    sendAuth(RequestType.DELETE_WAREHOUSE, GSON.toJson(id));
  }

  public List<StockResponse> getAllStock() {
    return parseList(sendAuth(RequestType.GET_STOCK, null), StockResponse.class);
  }

  public List<StockResponse> getStockByLocation(UUID locationId) {
    return parseList(
        sendAuth(RequestType.GET_STOCK_BY_LOCATION, GSON.toJson(locationId)), StockResponse.class);
  }

  public StockResponse setStock(SetStockRequest req) {
    return parse(sendAuth(RequestType.SET_STOCK, GSON.toJson(req)), StockResponse.class);
  }

  public List<ProductResponse> getProducts() {
    return parseList(sendAuth(RequestType.GET_PRODUCTS, null), ProductResponse.class);
  }

  public ProductResponse getProduct(UUID id) {
    return parse(sendAuth(RequestType.GET_PRODUCT, GSON.toJson(id)), ProductResponse.class);
  }

  public ProductResponse getProductByArticle(String article) {
    return parse(
        sendAuth(RequestType.GET_PRODUCT_BY_ARTICLE, GSON.toJson(article)), ProductResponse.class);
  }

  public ProductResponse createProduct(CreateProductRequest req) {
    return parse(sendAuth(RequestType.CREATE_PRODUCT, GSON.toJson(req)), ProductResponse.class);
  }

  public ProductResponse updateProduct(UpdateProductRequest req) {
    return parse(sendAuth(RequestType.UPDATE_PRODUCT, GSON.toJson(req)), ProductResponse.class);
  }

  public void deleteProduct(UUID id) {
    sendAuth(RequestType.DELETE_PRODUCT, GSON.toJson(id));
  }

  public WarehouseResponse deactivateWarehouse(UUID id) {
    return parse(
        sendAuth(RequestType.DEACTIVATE_WAREHOUSE, GSON.toJson(id)), WarehouseResponse.class);
  }

  public WarehouseResponse activateWarehouse(UUID id) {
    return parse(
        sendAuth(RequestType.ACTIVATE_WAREHOUSE, GSON.toJson(id)), WarehouseResponse.class);
  }

  public List<SaleResponse> getSalesByStore(UUID storeId) {
    return parseList(
        sendAuth(RequestType.GET_SALES_BY_STORE, GSON.toJson(storeId)), SaleResponse.class);
  }

  public SaleResponse processSale(CreateSaleRequest req) {
    return parse(sendAuth(RequestType.PROCESS_SALE, GSON.toJson(req)), SaleResponse.class);
  }

  public SaleResponse processReturn(UUID originalSaleId) {
    return parse(
        sendAuth(RequestType.PROCESS_RETURN, GSON.toJson(originalSaleId)), SaleResponse.class);
  }

  public List<TimesheetResponse> getAllTimesheetsByPeriod(ReportRequest req) {
    return parseList(
        sendAuth(RequestType.GET_ALL_TIMESHEETS_BY_PERIOD, GSON.toJson(req)),
        TimesheetResponse.class);
  }

  public List<SalesReportEntry> reportSales(ReportRequest req) {
    return parseList(sendAuth(RequestType.REPORT_SALES, GSON.toJson(req)), SalesReportEntry.class);
  }

  public List<EmployeeStats> reportEmployeeEfficiency(ReportRequest req) {
    return parseList(
        sendAuth(RequestType.REPORT_EMPLOYEE_EFFICIENCY, GSON.toJson(req)), EmployeeStats.class);
  }

  public List<StoreEfficiencyReport> reportStoreEfficiency(ReportRequest req) {
    return parseList(
        sendAuth(RequestType.REPORT_STORE_EFFICIENCY, GSON.toJson(req)),
        StoreEfficiencyReport.class);
  }

  private Response sendAuth(RequestType type, String payload) {
    return session.getClient().send(new Request(type, session.getToken(), payload));
  }

  private <T> T parse(Response resp, Class<T> clazz) {
    return GSON.fromJson(resp.getData(), clazz);
  }

  private <T> List<T> parseList(Response resp, Class<T> clazz) {
    Type type = TypeToken.getParameterized(List.class, clazz).getType();
    return GSON.fromJson(resp.getData(), type);
  }
}
