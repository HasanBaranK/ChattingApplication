import Common.Configuration;
import GUI.MainPage;
import MessageClasses.ListenUsers;
import MessageClasses.Message;

import java.io.DataInputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

public class Main {


    static int port = Configuration.listeningPort;
    /**
     * The Server.
     */
    static ServerSocket server;


    public static void main(String args[]) {

        //Open the GUI
        MainPage mainPage = new MainPage();
        mainPage.setVisible(true);

        //Thread for checking beacons
        Beacon beacon = new Beacon();
        Thread thread = new Thread(beacon);
        thread.start();

        //Thread for checking offline MessageClasses and sending them when users are online
        new Thread(Message::checkOfflineMessages).start();


        startServer();

        // running infinite loop for getting
        // client request
        while (true) {
            Socket s = null;

            try {
                // socket object to receive incoming client requests
                s = server.accept();

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());

                // create a new thread object
                Thread t = new ListenUsers(s);

                // Invoking the start() method
                t.start();

            } catch (Exception e) {
                try {
                    s.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }

    }

    /**
     * Method for starting the server.
     */
    public static void startServer() {

        try {
            server = new ServerSocket(port); // make a socket
            System.out.println("--* Starting server " + server.toString());
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }

    }
}
