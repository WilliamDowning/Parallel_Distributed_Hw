
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Establish connection with host
 * have routing table of fixed threads
 * take in array of length
 * implement merge sort with threads
 * return ordered array
 * Scenarios: 1T for N=10, 2T for N=100, 4T for N=1000, 4T for N=10000, 8T for N=10000, 16T for N=10000
 */
public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start_Server(){

        try{
            System.out.println("Listening to port: 5555");
            while(!serverSocket.isClosed()){
               clientSocket = serverSocket.accept();
               System.out.println("The Server has connected with its client at address: " + clientSocket.getInetAddress().getHostAddress());
            }
            serverSocket.close();
        }catch(Exception e){
            System.out.println("Failed to establish connection to Client from Server start_server");

        }

    }

    public static void main(String[] args){
        String clientIP = "192.168.1.114";
        try {
            ServerSocket ss = new ServerSocket(5555);
            Server server = new Server(ss);
            server.start_Server();

        }catch(IOException e){
            System.out.println("Failed to create Server Socket Connection at Server Main method");

        }
    }
}
