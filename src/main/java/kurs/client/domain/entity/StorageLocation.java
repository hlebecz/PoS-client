package kurs.client.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class StorageLocation {

  private UUID id;

  private String name;

  private LocalDateTime createdAt;

  private List<Stock> stock;
}
