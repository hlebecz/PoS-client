package kurs.client.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

  private UUID id;

  private Employee employee;

  private LocalDate workDate;

  private LocalTime checkIn;

  private LocalTime checkOut;

  private BigDecimal hoursWorked;
}
