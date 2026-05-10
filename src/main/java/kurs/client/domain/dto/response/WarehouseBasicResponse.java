package kurs.client.domain.dto.response;

import java.util.UUID;

import lombok.Data;

@Data
public class WarehouseBasicResponse {
  private UUID id;
  private String name;
}
