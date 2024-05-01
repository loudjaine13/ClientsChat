package Tp3;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server extends Thread {
    private HashMap<Integer, Integer[]> clientNeighbors; // HashMap to store client and its neighbors
    private int clientCount; // Counter to assign a unique number to each client
    private List<Conversation> conversations; // List to store active Conversation instances
    private Supervisor supervisor; // Supervisor thread instance

    public Server() {
        clientNeighbors = new HashMap<>();
        // Initializing client neighbors as per the provided information
        clientNeighbors.put(1, new Integer[]{2, 4});
        clientNeighbors.put(2, new Integer[]{1, 3});
        clientNeighbors.put(3, new Integer[]{2, 4});
        clientNeighbors.put(4, new Integer[]{1, 3});
        clientCount = 0;
        conversations = new ArrayList<>();
        supervisor = new Supervisor();
    }

    public static void main(String[] args) {
        new Server().start();
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(1234)) {
            System.out.println("Le serveur est prêt");

            supervisor.start(); // Start the Supervisor thread

            while (true) {
                Socket s = ss.accept();
                clientCount++;
                Conversation conversation = new Conversation(s, clientCount);
                conversations.add(conversation); // Add Conversation instance to the list
                conversation.start();
                System.out.println("Client " + clientCount + " connected.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Conversation extends Thread {
        private Socket socket;
        private int numero;

        public Conversation(Socket s, int num) {
            this.socket = s;
            this.numero = num;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                while (true) {
                    String clientResponse = br.readLine();
                    if (clientResponse == null) {
                        break;
                    }
                    // Forward the message to neighbors
                    forwardMessage(clientResponse, numero);
                    // Send a notification to the Supervisor thread
                    supervisor.notifyNewMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Method to forward message to neighbors
        private void forwardMessage(String message, int sender) {
            // Get neighbors of the sender from the clientNeighbors HashMap
            Integer[] neighbors = clientNeighbors.get(sender);
            if (neighbors != null) {
                for (Integer neighbor : neighbors) {
                    // Check if the neighbor is connected
                    if (neighbor != null && neighbor != sender) {
                        // Forward the message to the connected neighbor
                        sendMessageToClient(message, neighbor);
                        // Print the forwarding information
                        System.out.println("Server forwarded message from Client " + sender + " to Client " + neighbor);
                    }
                }
            }
        }
     // Method to send message to a specific client
        private void sendMessageToClient(String message, int clientNumber) {
            try {
                // Find the socket associated with the clientNumber
                for (Conversation conversation : conversations) {
                    if (conversation.numero == clientNumber) {
                        OutputStream os = conversation.socket.getOutputStream();
                        PrintWriter pw = new PrintWriter(os, true);
                        pw.println("Client " + numero + ": " + message); // Indicate the sender
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Supervisor extends Thread {
        private int messageCount;
        private long startTime;
        private Object lock;

        public Supervisor() {
            messageCount = 0;
            startTime = System.currentTimeMillis();
            lock = new Object();
        }

        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    try {
                        // Wait until notified by Conversation threads
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Calculate ideal elapsed time
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("Ideal elapsed time: " + elapsedTime + " milliseconds");
                // Print real-time
                System.out.println("Real-time: " + System.currentTimeMillis() + " milliseconds");
                // Print message count
                System.out.println("Message count: " + messageCount);
            }
        }

        // Method to receive notification of a new message from Conversation threads
        public synchronized void notifyNewMessage() {
            messageCount++;
            // Notify the Supervisor thread to print the information
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}