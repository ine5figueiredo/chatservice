package org.academiadecodigo.bootcamp55.MultiChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientChat {

    private int port;
    private String host;
    private String nickname;

    private Socket clientSocket;

    public void start() {

        try {

            //Opening stream for user input
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            //Getting threads to deal with income messages
            ExecutorService incomeMessages = Executors.newFixedThreadPool(2);

            //Getting Host and Port to connect
            getUserInput(userInput);

            //Establish connection to Server
            establishConnection(userInput);

            while (true) {
                sendMessage(userInput);

                incomeMessages.submit(new ReadChatMessages(clientSocket));
            }


        } catch (SocketException exception) {
            System.out.println("Connection closed.");

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void getUserInput(BufferedReader userInput) throws IOException {

        //Asking and getting the host name
        System.out.print("Host to connect: ");
        host = userInput.readLine();

        //Asking and getting the port number
        System.out.print("Port: ");
        port = Integer.parseInt(userInput.readLine());
    }

    public void establishConnection(BufferedReader userInput) throws IOException {

        clientSocket = new Socket(host, port);

        if (clientSocket.isBound()) {
            System.out.println("Connection established to host: " + host + ", IP: " + clientSocket.getInetAddress()
                    + ", Port: " + port);

            System.out.println("Welcome to Loop Chat.\n" +
                    "It's like UniChat, but you receive a bunch of messages from yourself.");

            //Asking and setting the nickname
            System.out.print("Please choose a Nickname: ");
            nickname = userInput.readLine();


            //Giving that nickname to our thread
            Thread.currentThread().setName(nickname);

        } else {
            System.out.println("Failed to establish connection.");
        }
    }

    private void sendMessage(BufferedReader userInput) throws IOException {

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String message = userInput.readLine();

        if (message.equals("/quit")) {
            out.close();

            System.out.println("**Connection closed**");

        } else {
            out.println(message);
        }
    }

    public static void main(String[] args) {

        ClientChat client = new ClientChat();
        client.start();

    }

    public class ReadChatMessages implements Runnable {

        private Socket clientSocket;
        private BufferedReader in;

        public ReadChatMessages(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        @Override
        public void run() {

            try {

                while (true) {

                    String receivedMessage = in.readLine();

                    if (receivedMessage.equals("Closed session.")) {

                        clientSocket.close();
                    } else {

                        System.out.println("Message received: " + receivedMessage);
                    }
                }

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
