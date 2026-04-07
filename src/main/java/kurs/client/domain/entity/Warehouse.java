package kurs.client.domain.entity;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class Warehouse extends StorageLocation {

  private String phone;

  private Boolean isActive = true;

  private Location location;

  private List<Store> stores;
}
