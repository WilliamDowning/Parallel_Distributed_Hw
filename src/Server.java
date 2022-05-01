

import java.io.*;
import java.lang.System;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


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
    DataOutputStream dos;
    DataInputStream dis;
    byte[] data = new byte[1000000];
    int[] rArray = new int[1000000];


    public Server(ServerSocket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
    }

    public void start_Server(){

        try{
            System.out.println("Listening to port: 5555");
            while(!serverSocket.isClosed()){
               clientSocket = serverSocket.accept();
                dos = new DataOutputStream(this.clientSocket.getOutputStream());
                dis = new DataInputStream(this.clientSocket.getInputStream());
               System.out.println("The Server has connected with its client at address: " + clientSocket.getInetAddress().getHostAddress());
               serverSocket.close();
            }
        }catch(Exception e){
            System.out.println("Failed to establish connection to Client from Server start_server");

        }

    }


    public void sendArray() throws IOException {

        for(int i = 0; i < data.length; i++){
            data[i] = (byte)rArray[i];
        }
        sendData(dos, data);
        System.out.println("Sending File Data");
        pauseForPeer();

    }

    public void receiveArray() throws IOException {

        data = getData(dis);
        for(int i = 0; i < data.length; i++){
            rArray[i] = data[i];
        }
        System.out.println("Received file contents");

    }



    public static byte[] getData(DataInputStream dis) {
        int dataLength = 0;
        try {
            dataLength = dis.readInt();
            byte[] content = new byte[dataLength];
            dis.readFully(content, 0, dataLength);
            return content;
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


    public static void pauseForPeer() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  void runMerge(){

//        System.out.print("Input = [");
//        for (int each: rArray)
//            System.out.print(each+", ");
//        System.out.print("] \n" +"Input.length = " + rArray.length + '\n');


        // Test custom single-threaded merge sort (recursive merge) implementation
       long t = System.currentTimeMillis();
        Integer[] arr1 = new Integer[rArray.length];
        for(int i = 0; i < arr1.length; i++){
            arr1[i] = rArray[i];
        }

        Integer[] arr2 = Arrays.copyOf(arr1,arr1.length);
        MergeSort.mergeSort(arr1, 0, arr1.length-1);
        t = System.currentTimeMillis() - t;
        System.out.println("Time spent for custom single threaded recursive merge_sort(): " + t + "ms");

        //Test custom (multi-threaded) merge sort (recursive merge) implementation
        //long j = System.currentTimeMillis();
        MergeSort.threadedSort(arr2);
        //j = System.currentTimeMillis() - j;
//        System.out.print("Output = [");
//        for (Integer each: arr1)
//            System.out.print(each+", ");
//        System.out.print("]\n");

        //System.out.println("Time spent for custom multi threaded recursive merge_sort(): " + j + "ms");

        for(int i = 0; i < arr2.length; i++){
        rArray[i] = arr2[i];
    }
}



    public static void main(String[] args){
        int attempts = 20;
        try {

            for(int i = 0; i < attempts; i++) {
                ServerSocket ss = new ServerSocket(5555);
                Server server = new Server(ss);
                server.start_Server();
                server.receiveArray();
                server.runMerge();
                server.sendArray();
            }

        }catch(IOException e){
            System.out.println("Failed to create Server Socket Connection at Server Main method");

        }
    }

}

class MergeSort{

    // Assuming system has 4 logical processors
    private static final int MAX_THREADS = 4;

    // Custom Thread class with constructors
    private static class SortThreads extends Thread{
        SortThreads(Integer[] array, int begin, int end){
            super(()->{
                MergeSort.mergeSort(array, begin, end);
            });
            this.start();
        }
    }

    // Perform Threaded merge sort
    public static void threadedSort(Integer[] array){
        // For performance - get current time in millis before starting
        long time = System.currentTimeMillis();
        final int length = array.length;
        // Workload per thread (chunk_of_data) = total_elements/core_count
        // if the no of elements exactly go into no of available threads,
        // then divide work equally,
        // else if some remainder is present, then assume we have (actual_threads-1) available workers
        // and assign the remaining elements to be worked upon by the remaining 1 actual thread.
        boolean exact = length%MAX_THREADS == 0;
        int maxlim = exact? length/MAX_THREADS: length/(MAX_THREADS-1);
        // if workload is less and no more than 1 thread is required for work, then assign all to 1 thread
        maxlim = maxlim < MAX_THREADS? MAX_THREADS : maxlim;
        // To keep track of threads
        final ArrayList<SortThreads> threads = new ArrayList<>();
        // Since each thread is independent to work on its assigned chunk,
        // spawn threads and assign their working index ranges
        // ex: for 16 element list, t1 = 0-3, t2 = 4-7, t3 = 8-11, t4 = 12-15
        for(int i=0; i < length; i+=maxlim){
            int beg = i;
            int remain = (length)-i;
            int end = remain < maxlim? i+(remain-1): i+(maxlim-1);
            final SortThreads t = new SortThreads(array, beg, end);
            // Add the thread references to join them later
            threads.add(t);
        }
        for(Thread t: threads){
            try{
                // This implementation of merge requires, all chunks worked by threads to be sorted first.
                // so we wait until all threads complete
                t.join();
            } catch(InterruptedException ignored){}
        }
        // System.out.println("Merging k-parts array, where m number of parts are distinctly sorted by each Threads of available MAX_THREADS="+MAX_THREADS);
		/*
		The merge takes 2 parts at a time and merges them into 1,
		then again merges the resultant into next part and so on...until end
		For MAXLIMIT = 2 (2 elements per thread where total threads = 4, in a total of 4*2 = 8 elements)
		list1 = (beg, mid); list2 = (mid+1, end);
		1st merge = 0,0,1 (beg, mid, end)
		2nd merge = 0,1,3 (beg, mid, end)
		3rd merge = 0,3,5 (beg, mid, end)
		4th merge = 0,5,7 (beg, mid, end)
		*/
        for(int i=0; i < length; i+=maxlim){
            int mid = i == 0? 0 : i-1;
            int remain = (length)-i;
            int end = remain < maxlim? i+(remain-1): i+(maxlim-1);
            // System.out.println("Begin: "+0 + " Mid: "+ mid+ " End: "+ end + " MAXLIM = " + maxlim);
            merge(array, 0, mid, end);
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent for custom multi-threaded recursive merge_sort(): "+ time+ "ms");
    }

    // Typical recursive merge sort
    public static void mergeSort(Integer[] array, int begin, int end){
        if (begin<end){
            int mid = (begin+end)/2;
            mergeSort(array, begin, mid);
            mergeSort(array, mid+1, end);
            merge(array, begin, mid, end);
        }
    }

    //Typical 2-way merge
    public static void merge(Integer[] array, int begin, int mid, int end){
        Integer[] temp = new Integer[(end-begin)+1];

        int i = begin, j = mid+1;
        int k = 0;

        // Add elements from first half or second half based on whichever is lower,
        // do until one of the list is exhausted and no more direct one-to-one comparison could be made
        while(i<=mid && j<=end){
            if (array[i] <= array[j]){
                temp[k] = array[i];
                i+=1;
            }else{
                temp[k] = array[j];
                j+=1;
            }
            k+=1;
        }

        // Add remaining elements to temp array from first half that are left over
        while(i<=mid){
            temp[k] = array[i];
            i+=1; k+=1;
        }

        // Add remaining elements to temp array from second half that are left over
        while(j<=end){
            temp[k] = array[j];
            j+=1; k+=1;
        }

        for(i=begin, k=0; i<=end; i++,k++){
            array[i] = temp[k];
        }
    }
}
