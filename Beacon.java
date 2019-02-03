import Common.Configuration;
import Common.MulticastEndpoint;
import Common.UserList;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Beacon implements Runnable {

    //nneccessary variables for beacon class
    static Configuration Config = new Configuration();
    static String username_ = System.getProperty("user.name");
    static MulticastEndpoint multiCastEndPoint;
    private static UserList userList = new UserList();
    /**
     * Sends beacons every 5 seconds and listens all the time except while sending the beacon
     * */
    public void run() {

        multiCastEndPoint = new MulticastEndpoint(Config);

        multiCastEndPoint.join();

        //count of offline message send
        int count = 0;
        while (true) {

            //send beacon if it is online or unavailable
            if (Configuration.status.equals("online") || Configuration.status.equals("unavailable")) {
                SendBeacon();
                count = 0;
            }
            //this if statement only runs once when offline with the help of the count
            else if (Configuration.status.equals("offline") && count == 0) {
                SendBeacon();
                count++;
            }

            //get starting time
            long start = System.currentTimeMillis();
            long sleepTime = 0;

            // check beacons as long as beacon time 5000 milliseconds
            while (sleepTime < Configuration.beaconTime) {
                GetBeacon();
                long now = System.currentTimeMillis();
                sleepTime = now - start;
            }


        }


    }
    /**
     * Gets the beacon and updates the user List
     * */
    static void GetBeacon() {
        byte[] b = new byte[Config.msgSize_];
        if (multiCastEndPoint.rx(b) && b.length > 0) {
            //System.out.println("-> rx : " + new String(b).trim());
            String userBeacon = new String(b);
            userBeacon = userBeacon.trim();
            userList.updateUserList(userBeacon);
        }
    }
    /**
     * Send the beacon
     * */
    static void SendBeacon() {
        byte[] b = new byte[0];
        String h = Beacon(Configuration.status);
        try {
            b = h.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Problem: " + e.getMessage());
        }

        if (multiCastEndPoint.tx(b)) {
            //System.out.println("<- tx : " + new String(b));
        }
    }
    /**
     * Create the beacon
     * **/
    static String Beacon(String status) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        String now = sdf.format(new Date());
        //String s = now + "|" + username_ + "|" + c_.hostInfo_;
        //[<timestamp>][<username>][online][<fqdn>][<port>]
        String s = "[" + now + "]" + "[" + username_ + "]" + "[" + status + "]" + "[" + Configuration.hostInfo_ + "]" + "[" + Config.listeningPort + "]";
        return s;
    }
}
