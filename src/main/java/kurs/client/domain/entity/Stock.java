package kurs.client.domain.entity;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

  private StorageLocation storageLocation;

  private Product product;

  private Integer quantity = 0;

  private LocalDateTime updatedAt;

  private void touch() {
    this.updatedAt = LocalDateTime.now();
  }
}
