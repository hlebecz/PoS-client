package kurs.client.domain.dto.request;

import java.util.UUID;

import lombok.*;

import kurs.client.domain.entity.UserRole;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest extends BaseRequest {
  private UUID id;
  private String login; // null = не менять
  private String newPassword; // null = не менять
  private UserRole role; // null = не менять
  private Boolean isActive; // null = не менять

  @Override
  public void validate() {
    require(id, "id");
    requireMaxLength(login, 100, "login");
  }
}
