package kurs.client.domain.model;

import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

  private String requestId;
  private RequestType type;
  private String token;
  private String payload;

  public Request(RequestType type, String token, String payload) {
    this.requestId = UUID.randomUUID().toString(); // ← здесь
    this.type = type;
    this.token = token;
    this.payload = payload;
  }

  public Request(RequestType type, String payload) {
    this(type, null, payload);
  }
}
