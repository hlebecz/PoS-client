package kurs.client.ui;

import java.time.LocalDate;
import java.util.List;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import kurs.client.domain.dto.report.EmployeeStats;
import kurs.client.domain.dto.report.SalesReportEntry;
import kurs.client.domain.dto.report.StoreEfficiencyReport;
import kurs.client.domain.dto.request.ReportRequest;
import kurs.client.ui.component.BaseController;

public class ReportController extends BaseController {

  // ── Продажи
  @FXML private DatePicker salesFromPicker;
  @FXML private DatePicker salesToPicker;
  @FXML private TableView<SalesRow> salesTable;
  @FXML private TableColumn<SalesRow, String> colSalesStore;
  @FXML private TableColumn<SalesRow, String> colSalesCount;
  @FXML private TableColumn<SalesRow, String> colReturnsCount;
  @FXML private TableColumn<SalesRow, String> colGross;
  @FXML private TableColumn<SalesRow, String> colNet;

  // ── Сотрудники
  @FXML private DatePicker empFromPicker;
  @FXML private DatePicker empToPicker;
  @FXML private TableView<EmpEffRow> empTable;
  @FXML private TableColumn<EmpEffRow, String> colEmpName;
  @FXML private TableColumn<EmpEffRow, String> colEmpPos;
  @FXML private TableColumn<EmpEffRow, String> colHours;
  @FXML private TableColumn<EmpEffRow, String> colDays;
  @FXML private TableColumn<EmpEffRow, String> colLabor;
  @FXML private TableColumn<EmpEffRow, String> colSalesCnt;
  @FXML private TableColumn<EmpEffRow, String> colRevenue;
  @FXML private TableColumn<EmpEffRow, String> colKpi;

  // ── По точкам
  @FXML private DatePicker storeFromPicker;
  @FXML private DatePicker storeToPicker;
  @FXML private TableView<StoreEffRow> storeReportTable;
  @FXML private TableColumn<StoreEffRow, String> colRptStore;
  @FXML private TableColumn<StoreEffRow, String> colRptNet;
  @FXML private TableColumn<StoreEffRow, String> colRptSales;
  @FXML private TableColumn<StoreEffRow, String> colRptLabor;
  @FXML private TableColumn<StoreEffRow, String> colRptHours;
  @FXML private TableColumn<StoreEffRow, String> colRptRph;

  private final ObservableList<SalesRow> salesItems = FXCollections.observableArrayList();
  private final ObservableList<EmpEffRow> empItems = FXCollections.observableArrayList();
  private final ObservableList<StoreEffRow> storeItems = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    // Продажи
    colSalesStore.setCellValueFactory(new PropertyValueFactory<>("storeName"));
    colSalesCount.setCellValueFactory(new PropertyValueFactory<>("salesCount"));
    colReturnsCount.setCellValueFactory(new PropertyValueFactory<>("returnsCount"));
    colGross.setCellValueFactory(new PropertyValueFactory<>("grossRevenue"));
    colNet.setCellValueFactory(new PropertyValueFactory<>("netRevenue"));
    salesTable.setItems(salesItems);

    // Сотрудники
    colEmpName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
    colEmpPos.setCellValueFactory(new PropertyValueFactory<>("position"));
    colHours.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
    colDays.setCellValueFactory(new PropertyValueFactory<>("workdays"));
    colLabor.setCellValueFactory(new PropertyValueFactory<>("laborCost"));
    colSalesCnt.setCellValueFactory(new PropertyValueFactory<>("salesCount"));
    colRevenue.setCellValueFactory(new PropertyValueFactory<>("salesRevenue"));
    colKpi.setCellValueFactory(new PropertyValueFactory<>("efficiencyIndex"));
    empTable.setItems(empItems);

    // По точкам
    colRptStore.setCellValueFactory(new PropertyValueFactory<>("storeName"));
    colRptNet.setCellValueFactory(new PropertyValueFactory<>("netRevenue"));
    colRptSales.setCellValueFactory(new PropertyValueFactory<>("salesCount"));
    colRptLabor.setCellValueFactory(new PropertyValueFactory<>("laborCost"));
    colRptHours.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
    colRptRph.setCellValueFactory(new PropertyValueFactory<>("revenuePerHour"));
    storeReportTable.setItems(storeItems);
  }


  @FXML
  private void handleSalesReport() {
    ReportRequest req = buildRequest(salesFromPicker, salesToPicker);
    if (req == null) return;
    async(
        () -> {
          List<SalesReportEntry> data = api.reportSales(req);
          javafx.application.Platform.runLater(
              () -> salesItems.setAll(data.stream().map(SalesRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleEmpReport() {
    ReportRequest req = buildRequest(empFromPicker, empToPicker);
    if (req == null) return;
    async(
        () -> {
          List<EmployeeStats> data = api.reportEmployeeEfficiency(req);
          javafx.application.Platform.runLater(
              () -> empItems.setAll(data.stream().map(EmpEffRow::from).toList()));
        },
        null);
  }

  @FXML
  private void handleStoreReport() {
    ReportRequest req = buildRequest(storeFromPicker, storeToPicker);
    if (req == null) return;
    async(
        () -> {
          List<StoreEfficiencyReport> data = api.reportStoreEfficiency(req);
          javafx.application.Platform.runLater(
              () -> storeItems.setAll(data.stream().map(StoreEffRow::from).toList()));
        },
        null);
  }


  private ReportRequest buildRequest(DatePicker from, DatePicker to) {
    LocalDate f = from.getValue();
    LocalDate t = to.getValue();
    if (f == null || t == null) {
      showError("Выберите период");
      return null;
    }
    if (f.isAfter(t)) {
      showError("Дата «С» позже даты «По»");
      return null;
    }
    return new ReportRequest(f, t, null);
  }

  private static String s(Object o) {
    return o != null ? o.toString() : "—";
  }


  public static class SalesRow {
    private final String storeName, salesCount, returnsCount, grossRevenue, netRevenue;

    public static SalesRow from(SalesReportEntry e) {
      return new SalesRow(
          s(e.storeName()),
          s(e.totalSalesCount()),
          s(e.totalReturnsCount()),
          s(e.grossRevenue()),
          s(e.netRevenue()));
    }

    public SalesRow(String sn, String sc, String rc, String gr, String nr) {
      storeName = sn;
      salesCount = sc;
      returnsCount = rc;
      grossRevenue = gr;
      netRevenue = nr;
    }

    public String getStoreName() {
      return storeName;
    }

    public String getSalesCount() {
      return salesCount;
    }

    public String getReturnsCount() {
      return returnsCount;
    }

    public String getGrossRevenue() {
      return grossRevenue;
    }

    public String getNetRevenue() {
      return netRevenue;
    }
  }

  public static class EmpEffRow {
    private final String fullName,
        position,
        totalHours,
        workdays,
        laborCost,
        salesCount,
        salesRevenue,
        efficiencyIndex;

    public static EmpEffRow from(EmployeeStats e) {
      return new EmpEffRow(
          s(e.fullName()),
          s(e.position()),
          s(e.totalHoursWorked()),
          s(e.workdaysCount()),
          s(e.totalLaborCost()),
          s(e.salesCount()),
          s(e.salesRevenue()),
          s(e.efficiencyIndex()));
    }

    public EmpEffRow(
        String fn, String pos, String th, String wd, String lc, String sc, String sr, String ei) {
      fullName = fn;
      position = pos;
      totalHours = th;
      workdays = wd;
      laborCost = lc;
      salesCount = sc;
      salesRevenue = sr;
      efficiencyIndex = ei;
    }

    public String getFullName() {
      return fullName;
    }

    public String getPosition() {
      return position;
    }

    public String getTotalHours() {
      return totalHours;
    }

    public String getWorkdays() {
      return workdays;
    }

    public String getLaborCost() {
      return laborCost;
    }

    public String getSalesCount() {
      return salesCount;
    }

    public String getSalesRevenue() {
      return salesRevenue;
    }

    public String getEfficiencyIndex() {
      return efficiencyIndex;
    }
  }

  public static class StoreEffRow {
    private final String storeName, netRevenue, salesCount, laborCost, totalHours, revenuePerHour;

    public static StoreEffRow from(StoreEfficiencyReport r) {
      return new StoreEffRow(
          s(r.storeName()),
          s(r.netRevenue()),
          s(r.salesCount()),
          s(r.totalLaborCost()),
          s(r.totalHoursWorked()),
          s(r.revenuePerHour()));
    }

    public StoreEffRow(String sn, String nr, String sc, String lc, String th, String rph) {
      storeName = sn;
      netRevenue = nr;
      salesCount = sc;
      laborCost = lc;
      totalHours = th;
      revenuePerHour = rph;
    }

    public String getStoreName() {
      return storeName;
    }

    public String getNetRevenue() {
      return netRevenue;
    }

    public String getSalesCount() {
      return salesCount;
    }

    public String getLaborCost() {
      return laborCost;
    }

    public String getTotalHours() {
      return totalHours;
    }

    public String getRevenuePerHour() {
      return revenuePerHour;
    }
  }
}
