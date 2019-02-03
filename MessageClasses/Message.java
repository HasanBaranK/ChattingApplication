package MessageClasses;

import Common.Configuration;
import Common.FileOperations;
import Common.UserList;

import java.net.InetAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static javax.swing.JOptionPane.showMessageDialog;

public class Message {

    //username
    static String username_ = System.getProperty("user.name");

    /**
     * A method for sending messages
     *
     * @param userId user you want to send to
     * @param fqdn fully qualified domain name of the user
     * @param port port of the user
     * @param text the message you would like to send
     * @param type type of the message you would like to send
     * */
    public static void sendMessage(String userId, String fqdn, String port, String text, String type) {
        //Dont send if you are offline
        if (Configuration.status.equals("online")) {
            //dont send if the text is empty
            if (!text.trim().equals("")) {

                //check sending was succusfull
                boolean success = true;

                Socket connection = null;
                try {
                    //get the ip of the user
                    String ip = InetAddress.getByName(fqdn).getHostAddress();
                    //connect to user
                    connection = ConnectUser(ip, port);

                    OutputStream tx = connection.getOutputStream();
                    byte[] buffer;
                    if (type.equals("unavailable")) {
                        buffer = UnavailableMessage().getBytes();
                    } else {
                        buffer = CreateMessage(text, type).getBytes();
                    }
                    int b = buffer.length;

                    if (b > 0) {
                        tx.write(buffer, 0, b); // send to server
                        System.out.println("Sending " + b + " bytes");
                    }


                    connection.close();
                } catch (IOException e) {
                    //if failed show it to the user
                    success = false;
                    showMessageDialog(null, "Something went wrong user might be unavailable");
                    System.out.println("Something went wrong user might be unavailable");
                }
                //if succesfull write it in to files and sho it to the user
                if (success) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//yyyyMMdd-HHmmss.SSS
                    Date date = new Date();
                    SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");//yyyyMMdd-HHmmss.SSS
                    Date date2 = new Date();
                    FileOperations.writeFile(userId + "Send", Configuration.MessagesFolderName, text + "\n" + formatter.format(date));
                    FileOperations.writeFileOverride(Configuration.LastConversationsFileName, Configuration.InfoFolderName, formatter2.format(date2) + "," + userId + "," + text);
                }
            } else {
                showMessageDialog(null, "You cant send empty messages");
            }
        } else {
            showMessageDialog(null, "You have to be online to send messages");
        }
    }
    /**
     * An overloaded version of the method mostly used when sending unavaliable and stored text messages
     * This one does not writes it inside the files so it does not need a username
     *
     * @param fqdn fully qualified domain name of the user
     * @param port port of the user
     * @param text the message you would like to send
     * @param type type of the message you would like to send
     * */
    public static void sendMessage(String fqdn, String port, String text, String type) {


        try {
            // get the ip address
            String ip = InetAddress.getByName(fqdn).getHostAddress();
            Socket connection = ConnectUser(ip, port);
            OutputStream tx = connection.getOutputStream();
            byte[] buffer;
            if (type.equals("unavailable")) {
                buffer = UnavailableMessage().getBytes();
            } else {
                buffer = CreateMessage(text, type).getBytes();
            }
            int b = buffer.length;

            if (b > 0) {
                tx.write(buffer, 0, b); // send to server
                System.out.println("Sending " + b + " bytes");
            }
            connection.close();
        } catch (IOException e) {
            //if fails show message because it might be stored text
            if(!type.equals("unavailable")){
            showMessageDialog(null, "Could not end the text:"+text+"Something went wrong user might be unavailable");
            }else{
                System.out.println("Something went wrong user might be unavailable");
            }
        }

    }


    /**
     * Check the offline messages o the user every 5 seconds if the user is online send the message
     *
     * */
    public static void checkOfflineMessages() {
        while (true) {

            //read file in to line
            String[] lines = FileOperations.readFile(Configuration.MessagesFolderName, Configuration.OfflineMessagesFileName);
            for (String line : lines) {
                String[] lineSplitted = line.split(",");
                if (lineSplitted.length == 5) {
                    if (UserList.checkStatus(lineSplitted[1]).equals("online")) {
                        //Waiting the user to see me online
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Send the message
                        sendMessage(lineSplitted[2], lineSplitted[3], lineSplitted[4], "stored_text");
                        //deletes the specific line limits with one line messages when sending offline messages
                        FileOperations.deleteLine(Configuration.MessagesFolderName, Configuration.OfflineMessagesFileName, line);
                    }
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

            }
        }
    }
    /**
     * Connect the user
     *
     * @param hostname hostname of the user
     * @param portNumber port number of the user
     * */
    private static Socket ConnectUser(String hostname, String portNumber) throws IOException {

        int port = Integer.parseInt(portNumber);
        Socket connection = new Socket(hostname, port); // server
        System.out.println("--* Connecting to " + connection.toString());
        return connection;
    }
    /**
     * Create a message
     * @param text  the message you want to send
     * @param type the type of the message
     * */
    public static String CreateMessage(String text, String type) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        String now = sdf.format(new Date());
        String s = "[" + now + "]" + "[" + username_ + "]" + "[" + type + "]" + "[" + text + "]";
        return s;

    }
    /**
     * Create an unavalable message
     * */
    public static String UnavailableMessage() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        String now = sdf.format(new Date());
        String s = "[" + now + "]" + "[" + username_ + "]" + "[unavailable]";
        // <timestamp>][<username>][unavailable]
        return s;

    }

}
