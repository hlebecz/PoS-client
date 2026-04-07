package kurs.client.network;

public class ServerException extends RuntimeException {
  private final String errorCode;

  public ServerException(String message) {
    super(message);
    this.errorCode = "UNKNOWN";
  }

  public ServerException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
