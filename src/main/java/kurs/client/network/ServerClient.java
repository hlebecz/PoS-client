package kurs.client.network;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;

import kurs.client.domain.model.Request;
import kurs.client.domain.model.Response;

public class ServerClient {

  private static final Gson GSON = new Gson();

  private final String host;
  private final int port;

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  public ServerClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public Response send(Request request) {
    try {
      ensureConnected();
      return doSend(request);
    } catch (IOException e) {
      closeQuietly();
      try {
        connect();
        return doSend(request);
      } catch (IOException ex) {
        throw new ServerException("Нет соединения с сервером: " + ex.getMessage());
      }
    }
  }

  public void connect() throws IOException {
    socket = new Socket(host, port);
    socket.setSoTimeout(10000);
    out = new PrintWriter(socket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
  }

  public void disconnect() {
    closeQuietly();
  }

  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }

  private Response doSend(Request request) throws IOException {
    out.println(GSON.toJson(request));
    String line = in.readLine();
    if (line == null) throw new IOException("Соединение закрыто сервером");
    Response response = GSON.fromJson(line, Response.class);
    if (!response.isSuccess())
      throw new ServerException(
          response.getMessage() != null ? response.getMessage() : "Ошибка сервера",
          response.getErrorCode());
    return response;
  }

  private void ensureConnected() throws IOException {
    if (!isConnected()) connect();
  }

  private void closeQuietly() {
    try {
      if (socket != null) socket.close();
    } catch (IOException ignored) {
    }
    socket = null;
    out = null;
    in = null;
  }
}
