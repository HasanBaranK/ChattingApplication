package MessageClasses;

import Common.Configuration;
import Common.FileOperations;
import Common.UserList;
import GUI.MainPage;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class which runs in a separate thread and listens users messages and reacts to users messages
 */

public class ListenUsers extends Thread {

    static int bufferSize = 140; // a linestatic int bufferSize = 140; // a line
    private Socket connection;

    public ListenUsers(Socket connection) {

        try {
            //finding the fqdn of the user
            String fqdn = connection.getInetAddress().getLocalHost().getHostName();

            if (Configuration.status.equals("unavailable")) {

                String port = String.valueOf(connection.getPort());
                Message.sendMessage(fqdn, port, "", "unavailable");
                connection.close();

            } else {

                InputStream rx = connection.getInputStream();
                System.out.println("A New User Connected");

                //read the message of the client
                byte[] buffer = new byte[bufferSize];
                int b = 0;
                //wait until there is an answer if b=-1 that means connection closed
                while (b < 1) {
                    buffer = new byte[bufferSize];
                    try {
                        b = rx.read(buffer);

                        if (b == -1) {
                            connection.close();
                            System.out.println("Connection closed");
                            return;
                        }
                    } catch (IOException e) {
                        //ignore
                    }
                }


                String s = new String(buffer);
                s = s.trim();
                //splitting the message
                String[] messageSplitted = s.split("]");
                if (messageSplitted.length > 3) {
                    messageSplitted[0] = messageSplitted[0].replace("[", "");
                    messageSplitted[1] = messageSplitted[1].replace("[", "");
                    messageSplitted[1] = messageSplitted[1].trim();
                    messageSplitted[2] = messageSplitted[2].replace("[", "");
                    messageSplitted[3] = messageSplitted[3].replace("[", "");
                }
                //if user is online and I am online and message != empty than print and show the message
                if (UserList.checkStatus(messageSplitted[1]).equals("online") && !messageSplitted[3].equals("") && Configuration.status.equals("online")) {
                    //Notifies the user
                    MainPage.Notify(messageSplitted[1] + " : " + messageSplitted[3]);

                    //format the date and time to usable to show time stamp in gui
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    //Write the received messages in the indiavidual folder of that users received messages
                    FileOperations.writeFile(messageSplitted[1] + "Received", Configuration.MessagesFolderName, messageSplitted[3] + "\n" + formatter.format(date));

                    //I have tried using html css for showing the messages in a messenge view by making them invisible but visibility hidden was not supported

                    //String invisibleText = "<html><p style=\"visibility:hidden\">" + messageSplitted[3] + "\n" + formatter.format(date) + "</p></html>";
                    //Common.FileOperations.writeFile(messageSplitted[1] + "Send", "MessageClasses",invisibleText);

                    //Override the Last conversations file
                    FileOperations.writeFileOverride(Configuration.LastConversationsFileName, Configuration.InfoFolderName, messageSplitted[0] + "," + messageSplitted[1] + "," + messageSplitted[3]);
                    System.out.println("Received " + b + " bytes --> " + s);
                }
            }
        } catch (UnknownHostException e) {
            //we don`t know the origin of the host
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
