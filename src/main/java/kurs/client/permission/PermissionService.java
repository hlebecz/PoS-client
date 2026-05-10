package kurs.client.permission;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import kurs.client.domain.entity.UserRole;

public class PermissionService {

  private static final Map<ViewName, Map<UserRole, Set<PermissionAction>>> PERMISSIONS =
      new EnumMap<>(ViewName.class);

  static {
    // Products View
    Map<UserRole, Set<PermissionAction>> products = new EnumMap<>(UserRole.class);
    products.put(UserRole.GUEST, EnumSet.of(PermissionAction.DISPLAY));
    products.put(UserRole.CASHIER, EnumSet.of(PermissionAction.DISPLAY));
    products.put(UserRole.ACCOUNTANT, EnumSet.of(PermissionAction.DISPLAY));
    products.put(UserRole.MANAGER, EnumSet.of(PermissionAction.DISPLAY));
    products.put(
        UserRole.ADMIN,
        EnumSet.of(
            PermissionAction.DISPLAY,
            PermissionAction.CREATE,
            PermissionAction.UPDATE,
            PermissionAction.DELETE));
    PERMISSIONS.put(ViewName.PRODUCTS, products);

    // Stores View
    Map<UserRole, Set<PermissionAction>> stores = new EnumMap<>(UserRole.class);
    stores.put(UserRole.GUEST, EnumSet.noneOf(PermissionAction.class));
    stores.put(UserRole.CASHIER, EnumSet.noneOf(PermissionAction.class));
    stores.put(UserRole.ACCOUNTANT, EnumSet.noneOf(PermissionAction.class));
    stores.put(
        UserRole.MANAGER,
        EnumSet.of(PermissionAction.DISPLAY, PermissionAction.UPDATE)); // Manager can only update
    stores.put(
        UserRole.ADMIN,
        EnumSet.of(
            PermissionAction.DISPLAY,
            PermissionAction.CREATE,
            PermissionAction.UPDATE,
            PermissionAction.DELETE));
    PERMISSIONS.put(ViewName.STORES, stores);

    // Warehouses View
    Map<UserRole, Set<PermissionAction>> warehouses = new EnumMap<>(UserRole.class);
    warehouses.put(UserRole.GUEST, EnumSet.noneOf(PermissionAction.class));
    warehouses.put(UserRole.CASHIER, EnumSet.noneOf(PermissionAction.class));
    warehouses.put(UserRole.ACCOUNTANT, EnumSet.of(PermissionAction.DISPLAY));
    warehouses.put(
        UserRole.MANAGER,
        EnumSet.of(PermissionAction.DISPLAY, PermissionAction.CREATE, PermissionAction.UPDATE));
    warehouses.put(
        UserRole.ADMIN,
        EnumSet.of(
            PermissionAction.DISPLAY,
            PermissionAction.CREATE,
            PermissionAction.UPDATE,
            PermissionAction.DELETE));
    PERMISSIONS.put(ViewName.WAREHOUSES, warehouses);

    // Employees View
    Map<UserRole, Set<PermissionAction>> employees = new EnumMap<>(UserRole.class);
    employees.put(UserRole.GUEST, EnumSet.noneOf(PermissionAction.class));
    employees.put(UserRole.CASHIER, EnumSet.noneOf(PermissionAction.class));
    employees.put(UserRole.ACCOUNTANT, EnumSet.noneOf(PermissionAction.class));
    employees.put(
        UserRole.MANAGER,
        EnumSet.of(PermissionAction.DISPLAY, PermissionAction.CREATE, PermissionAction.UPDATE));
    employees.put(
        UserRole.ADMIN,
        EnumSet.of(
            PermissionAction.DISPLAY,
            PermissionAction.CREATE,
            PermissionAction.UPDATE,
            PermissionAction.DELETE));
    PERMISSIONS.put(ViewName.EMPLOYEES, employees);

    // Users View
    Map<UserRole, Set<PermissionAction>> users = new EnumMap<>(UserRole.class);
    users.put(UserRole.GUEST, EnumSet.noneOf(PermissionAction.class));
    users.put(UserRole.CASHIER, EnumSet.noneOf(PermissionAction.class));
    users.put(UserRole.ACCOUNTANT, EnumSet.noneOf(PermissionAction.class));
    users.put(UserRole.MANAGER, EnumSet.noneOf(PermissionAction.class));
    users.put(
        UserRole.ADMIN,
        EnumSet.of(
            PermissionAction.DISPLAY,
            PermissionAction.CREATE,
            PermissionAction.UPDATE,
            PermissionAction.DELETE));
    PERMISSIONS.put(ViewName.USERS, users);

    // Stock View
    Map<UserRole, Set<PermissionAction>> stock = new EnumMap<>(UserRole.class);
    stock.put(UserRole.GUEST, EnumSet.of(PermissionAction.DISPLAY));
    stock.put(UserRole.CASHIER, EnumSet.of(PermissionAction.DISPLAY));
    stock.put(UserRole.ACCOUNTANT, EnumSet.of(PermissionAction.DISPLAY, PermissionAction.UPDATE));
    stock.put(UserRole.MANAGER, EnumSet.of(PermissionAction.DISPLAY, PermissionAction.UPDATE));
    stock.put(
        UserRole.ADMIN,
        EnumSet.of(
            PermissionAction.DISPLAY,
            PermissionAction.CREATE,
            PermissionAction.UPDATE,
            PermissionAction.DELETE));
    PERMISSIONS.put(ViewName.STOCK, stock);

    // Sales View
    Map<UserRole, Set<PermissionAction>> sales = new EnumMap<>(UserRole.class);
    sales.put(UserRole.GUEST, EnumSet.noneOf(PermissionAction.class));
    sales.put(
        UserRole.CASHIER,
        EnumSet.of(PermissionAction.DISPLAY, PermissionAction.CREATE, PermissionAction.UPDATE));
    sales.put(
        UserRole.ACCOUNTANT,
        EnumSet.of(PermissionAction.DISPLAY, PermissionAction.CREATE, PermissionAction.UPDATE));
    sales.put(
        UserRole.MANAGER,
        EnumSet.of(PermissionAction.DISPLAY, PermissionAction.CREATE, PermissionAction.UPDATE));
    sales.put(
        UserRole.ADMIN,
        EnumSet.of(
            PermissionAction.DISPLAY,
            PermissionAction.CREATE,
            PermissionAction.UPDATE,
            PermissionAction.DELETE));
    PERMISSIONS.put(ViewName.SALES, sales);

    // Reports View
    Map<UserRole, Set<PermissionAction>> reports = new EnumMap<>(UserRole.class);
    reports.put(UserRole.GUEST, EnumSet.noneOf(PermissionAction.class));
    reports.put(UserRole.CASHIER, EnumSet.noneOf(PermissionAction.class));
    reports.put(UserRole.ACCOUNTANT, EnumSet.of(PermissionAction.DISPLAY));
    reports.put(UserRole.MANAGER, EnumSet.noneOf(PermissionAction.class));
    reports.put(UserRole.ADMIN, EnumSet.of(PermissionAction.DISPLAY));
    PERMISSIONS.put(ViewName.REPORTS, reports);
  }

  /**
   * Check if the given role has permission to perform the specified action on the view.
   *
   * @param role the user role
   * @param view the view name
   * @param action the permission action
   * @return true if the role has permission, false otherwise
   */
  public static boolean hasPermission(UserRole role, ViewName view, PermissionAction action) {
    if (role == null || view == null || action == null) {
      return false;
    }

    Map<UserRole, Set<PermissionAction>> viewPermissions = PERMISSIONS.get(view);
    if (viewPermissions == null) {
      return false;
    }

    Set<PermissionAction> rolePermissions = viewPermissions.get(role);
    if (rolePermissions == null) {
      return false;
    }

    return rolePermissions.contains(action);
  }

  /**
   * Check if the given role can display the view.
   *
   * @param role the user role
   * @param view the view name
   * @return true if the role can display the view
   */
  public static boolean canDisplay(UserRole role, ViewName view) {
    return hasPermission(role, view, PermissionAction.DISPLAY);
  }

  /**
   * Check if the given role can create entities in the view.
   *
   * @param role the user role
   * @param view the view name
   * @return true if the role can create entities
   */
  public static boolean canCreate(UserRole role, ViewName view) {
    return hasPermission(role, view, PermissionAction.CREATE);
  }

  /**
   * Check if the given role can update entities in the view.
   *
   * @param role the user role
   * @param view the view name
   * @return true if the role can update entities
   */
  public static boolean canUpdate(UserRole role, ViewName view) {
    return hasPermission(role, view, PermissionAction.UPDATE);
  }

  /**
   * Check if the given role can delete entities in the view.
   *
   * @param role the user role
   * @param view the view name
   * @return true if the role can delete entities
   */
  public static boolean canDelete(UserRole role, ViewName view) {
    return hasPermission(role, view, PermissionAction.DELETE);
  }
}
