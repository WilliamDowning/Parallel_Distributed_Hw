
import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client {
    DataOutputStream dos;
    DataInputStream dis;
    private Socket socket;
    byte[] data = new byte[1000000];
    int[] input = new int[1000000];
    long fileLength;




    public Client(Socket socket) throws IOException {
        this.socket = socket;
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());

    }

    private void createArray(){

            for(int i = 0; i < input.length; i++) {
                input[i] = (int) (Math.random() * 100);

            }
    }

    public static byte[] getData(DataInputStream dis) {
        int dataLength = 0;
        try {
            dataLength = dis.readInt();
            byte[] data = new byte[dataLength];
            dis.readFully(data, 0, dataLength);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static void sendData(DataOutputStream dos, byte[] data) {
        try {
            dos.writeInt(data.length);
            dos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendArray() throws IOException {

        for(int i = 0; i < data.length; i++){
            data[i] = (byte)input[i];
        }
        sendData(dos, data);
        System.out.println("Sending array Data");

    }

    public void receiveSortedArray() throws IOException {

        data = getData(dis);
        for(int i = 0; i < data.length; i++){
            input[i] = data[i];
        }
        System.out.println("Received file contents");
//        for(int i = 0; i < input.length; i++){
//            System.out.print(input[i]+ "\n");
//        }
    }

    public static void pauseForPeer() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }






    public static void main(String[] args) throws FileNotFoundException {
        int attempts = 20;
        long time;
        try {
            for(int i = 0; i < attempts; i++) {
                System.out.println("Currently on Attempt: " + (i + 1));
                time = System.currentTimeMillis();
                Socket clientSocket = new Socket("localhost", 5555);
                Client client = new Client(clientSocket);
                client.createArray();
                client.sendArray();
                pauseForPeer();
                client.receiveSortedArray();
                time = System.currentTimeMillis() - time;
                System.out.println("Time Taken for Attempt to receive Sorted Array: " + time + " ms");
                pauseForPeer();
            }

        }catch(IOException e){
            System.out.println("Failed to make connection to Server from Client main method");
        }

        System.out.println("Successfully communicated with server");

    }

}

