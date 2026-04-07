package kurs.client.session;

import lombok.Getter;

import kurs.client.domain.entity.UserRole;
import kurs.client.network.ServerClient;

@Getter
public class Session {

  private static final Session INSTANCE = new Session();

  private ServerClient client;
  private String token;
  private String login;
  private UserRole role;

  private Session() {}

  public static Session getInstance() {
    return INSTANCE;
  }

  public void open(ServerClient client, String token, String login, String roleName) {
    this.client = client;
    this.token = token;
    this.login = login;
    this.role = UserRole.valueOf(roleName);
  }

  public void close() {
    token = null;
    login = null;
    role = null;
    if (client != null) {
      client.disconnect();
      client = null;
    }
  }

  public boolean isOpen() {
    return token != null;
  }

  public boolean isAdmin() {
    return role == UserRole.ADMIN;
  }

  public boolean isManager() {
    return role == UserRole.MANAGER;
  }

  public boolean isAccountant() {
    return role == UserRole.ACCOUNTANT;
  }

  public boolean isCashier() {
    return role == UserRole.CASHIER;
  }

  public boolean isGuest() {
    return role == UserRole.GUEST;
  }
}
