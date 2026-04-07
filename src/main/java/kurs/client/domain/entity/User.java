package kurs.client.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  private UUID id;

  private String login;

  private String passwordHash;

  private UserRole role;

  private Boolean isActive = true;

  private LocalDateTime createdAt;
}
