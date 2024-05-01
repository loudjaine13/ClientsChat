package Tp3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1234);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    while (true) {
                        String message = br.readLine();
                        if (message != null) {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Start a thread to send messages to the server
            new Thread(() -> {
                try {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        System.out.println("Client: ");
                        String message = scanner.nextLine();
                        String messageWithTime = message + " - Sent at: " + System.currentTimeMillis();
                        pw.println(messageWithTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Start thread controllers (not implemented)
            Thread controllersThread = new Thread(new Controllers());
            controllersThread.start();

            // Start thread application (not implemented)
            Thread applicationThread = new Thread(new Application());
            applicationThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Controllers implements Runnable {
        @Override
        public void run() {
        	
        }
    }

    static class Application implements Runnable {
        @Override
        public void run() {
        	
        }
    }
}