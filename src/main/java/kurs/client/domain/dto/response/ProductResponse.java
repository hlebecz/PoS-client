package kurs.client.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
  private UUID id;
  private String name;
  private String article;
  private BigDecimal price;
  private LocalDateTime createdAt;
}
