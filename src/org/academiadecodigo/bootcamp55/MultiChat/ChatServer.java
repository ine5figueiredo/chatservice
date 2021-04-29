package org.academiadecodigo.bootcamp55.MultiChat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    //Passing a final port number
    private final int port;

    //Setting a new container
    private Vector<ServerWorker> serversList;

    //Saving messages to broadcast
    private String clientMessage;


    public ChatServer(int port) {
        this.port = port;
    }

    public void getConnection() {

        try {

            ServerSocket serverSocket = new ServerSocket(port);

            ExecutorService serverThreads = Executors.newFixedThreadPool(2);

            serversList = new Vector<>();


            while (true) {

                ServerWorker serverWorker = new ServerWorker(serverSocket.accept());

                //Adding the new connection to the list
                serversList.add(serverWorker);

                //Passing the new connection to a thread
                serverThreads.submit(serverWorker);

            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void sendMessageToAll(String clientMessage) {

        for (ServerWorker worker : serversList) {
                worker.sendMessage(clientMessage);
            }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(8989);
        server.getConnection();
    }

    public class ServerWorker implements Runnable {

        private Socket connection;
        private BufferedReader in;
        private PrintWriter out;

        public ServerWorker(Socket connection) throws IOException {
            this.connection = connection;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new PrintWriter(connection.getOutputStream(), true);
        }

        @Override
        public void run() {

            try {
                establishConnection();

                readMessage();


            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }


        }

        public void establishConnection() throws IOException {

            if (connection.isBound()) {

                String clientAddress = connection.getInetAddress().getHostName();
                int clientPort = connection.getPort();

                System.out.println("Connected to: " + clientAddress + ", Port: " + clientPort);

            } else {
                System.err.println("Connection failed.");
            }
        }

        public void readMessage() throws IOException, InterruptedException {

            while (true) {

                clientMessage = in.readLine();

                if (clientMessage.equals("/quit")) {

                    sendMessageToAll(Thread.currentThread().getName() + "has left the chat");

                    sendMessage("Closed session.");

                    connection.close();

                } else {

                    sendMessageToAll(clientMessage);
                }
            }
        }

        public void sendMessage(String clientMessage) {
                out.println(clientMessage);
        }
    }
}
