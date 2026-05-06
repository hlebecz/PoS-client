package kurs.client.domain.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

import kurs.client.domain.entity.User;
import kurs.client.domain.entity.UserRole;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  private UUID id;
  private String login;
  private UserRole role;
  private boolean isActive;
  private UUID employeeId;
  private String employeeName;
  private LocalDateTime createdAt;

  public static UserResponse from(User u) {
    return UserResponse.builder()
        .id(u.getId())
        .login(u.getLogin())
        .role(u.getRole())
        .isActive(u.getIsActive())
        .employeeId(null)
        .employeeName(null)
        .createdAt(u.getCreatedAt())
        .build();
  }
}
