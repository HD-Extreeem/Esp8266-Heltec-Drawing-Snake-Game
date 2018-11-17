
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Date;


public class JavaHTTPServer{

    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(8888);
            System.out.println("Server started, Listening on port nbr : " + 8888);
            DrawingUI ui = new DrawingUI();


            while (true) {
                WebThread wThread = new WebThread(serverConnect.accept(),ui);
                System.out.println("Connecton opened. time/date: " + new Date());
                Thread thread = new Thread(wThread);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Server Connection error : " + e.getMessage());
        }
    }

}