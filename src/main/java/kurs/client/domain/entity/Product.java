package kurs.client.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  private UUID id;

  private String name;

  private String article;

  private BigDecimal price;

  private LocalDateTime createdAt;

  // Остатки этого товара по всем хранилищам

  private List<Stock> stock;
}
