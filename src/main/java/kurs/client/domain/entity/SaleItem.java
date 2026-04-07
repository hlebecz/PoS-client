package kurs.client.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem {

  private UUID id;

  private Sale sale;

  private Product product;

  private Integer quantity;

  private BigDecimal unitPrice;
}
