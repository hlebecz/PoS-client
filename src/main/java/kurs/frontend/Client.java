package kurs.frontend;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        try (
                Socket socket = new Socket(HOST, PORT);
                var out = new PrintWriter(socket.getOutputStream(), true);
                var in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to " + HOST + ":" + PORT);
            System.out.println("Type a message and press Enter. Type 'exit' to quit.");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("exit")) break;

                out.println(input);
                System.out.println("Server: " + in.readLine());
            }
        } catch (ConnectException e) {
            System.out.println("Could not connect to server at " + HOST + ":" + PORT);
        }
    }
}