import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private Socket socket;

    public Client(Socket socket){
        this.socket = socket;
    }


    public static void main(String[] args) {

        try {
            Socket clientSocket = new Socket("localhost", 5555);
            Client client = new Client(clientSocket);
            client.socket.close();
        }catch(IOException e){
            System.out.println("Failed to make connection to Server from Client main method");
        }

    }
}

