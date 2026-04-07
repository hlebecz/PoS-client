package kurs.client.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

  private UUID id;

  // 1:1 — сотрудник может иметь учётную запись

  private User user;

  private Store store;

  private String fullName;

  private String position;

  private BigDecimal hourlyRate;

  private String phone;

  private String email;

  private LocalDate hiredAt;

  private LocalDate firedAt;

  private Location location;

  private LocalDateTime createdAt;

  // Табель рабочего времени сотрудника

  private List<Timesheet> timesheets;
}
