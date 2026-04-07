package kurs.client.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

  private UUID id;

  private Store store;

  private Employee cashier;

  private BigDecimal total;

  private Boolean isReturn = false;

  private LocalDateTime soldAt;

  private List<SaleItem> items = new ArrayList<>();
}
