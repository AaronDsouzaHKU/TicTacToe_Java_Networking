
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class implements a server to play a two-player tic tac toe game.
 * The server receives messages when a player makes a move or when both
 * players want to restart the game.
 * The server sends messages to inform a player their player Number,
 * when the game should start or inform the players the moves made by
 * the other player.
 */

public class TicTacToeServer {
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static int prevPlayer=0;
    private static int startnum=0;
    
    /**
     * the main method starts a new server socket, and accepts client
     * sockets. It also sends the player their player Number when they join.
     * It starts the thread to handle each of the clients.
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        ServerSocket serverSock = new ServerSocket(5000);
        System.out.println("Server Started");

        while (true) {
            Socket clientSocket = serverSock.accept();
            ClientHandler handler = new ClientHandler(clientSocket);
            clients.add(handler);
            if(clients.size()<=2){
                prevPlayer=prevPlayer%2+1;
                String playerMessage="Player: "+String.valueOf(prevPlayer);
                
                System.out.println(playerMessage);
                handler.send(playerMessage);
                if(clients.size()==2) {
                    broadcast("start");
                } 
            }
            new Thread(handler).start();
        }

    }

    /**
     * Broadcasts or sends a move to all clients
     * @param move- the move or string to be broadcasted
     */
    public static void broadcast(String move) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.send(move);
            }
        }
    }
    
    /**
     * Inner class that handles each client connection.
     * It implements the Runnable class as the run 
     * method will be run on a seperate thread.
     */

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final PrintWriter out;
        
        /**
         * The constructor for ClientHandler that initialises
         * the client socket and the writer
         * @param socket- the client socket
         * @throws IOException - if an I/O error occurs when creating the output stream
         */

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }
        
        /**
         * sends the message to the current client. 
         * @param message- the string to be sent
         */

        public void send(String message) {
            out.println(message);
        }
        
        /**
         * Overriden function of runnable which runs on a thread.
         * It reads in the message it receives from the clients 
         * and broadcasts the messages when needed.
         */

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {

                String move;
                while ((move = in.readLine()) != null) {
                    if (move.length() == 2) {
                        broadcast(move);  
                        System.out.println("Broadcasted move: " + move);
                    }
                    else if (move.equals("restart")){
                        startnum++;
                        if(startnum%2==0){
                            System.out.println(move);
                            broadcast("start");
                        }
                    }
                }
            } catch (IOException e) {
            } finally {
                clients.remove(this);
                broadcast("LEFT");
                try { socket.close(); } catch (IOException e) {}
                System.out.println("Game Over");
                System.exit(0);
            }
        }
    }
}
