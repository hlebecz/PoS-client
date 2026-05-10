package kurs.client.domain.dto.response;

import java.util.UUID;

import lombok.Data;

@Data
public class StoreBasicResponse {
  private UUID id;
  private String name;
}
