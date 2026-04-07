package kurs.client.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

  private UUID id;

  private BigDecimal x;

  private BigDecimal y;

  private String address;

  private String city;

  private LocalDateTime createdAt;
}
