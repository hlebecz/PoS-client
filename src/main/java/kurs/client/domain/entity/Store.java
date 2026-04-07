package kurs.client.domain.entity;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class Store extends StorageLocation {

  private String phone;

  private Boolean isActive = true;

  private User manager;

  private Warehouse warehouse;

  private Location location;

  private List<Employee> employees;

  private List<Sale> sales;
}
