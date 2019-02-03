package Common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * A class for configuring everything
 */
public class Configuration {

    //General configurations
    public String mAddr_ = "239.42.42.42"; // CS2003 whole class gorup239.0.82.148
    public String mAddr_forAudio = "239.0.82.45"; //
    public int MsgSizeForAudio = 65536; // Maximum size a buffer can get
    public static int listeningPort = 51153; // random(ish) my own port 51153
    public int mPort_ = 10101; // general chat port
    public static String status = "online"; //
    public int ttl_ = 4; // plenty for the lab
    public int soTimeout_ = 1000; // ms
    public boolean loopbackOff_ = true; // ignore my own transmissions
    public boolean reuseAddr_ = true; // allow address use by other apps

    //Array list that contains notifications
    public static ArrayList<String> notifications = new ArrayList<>();

    // application config
    public int msgSize_ = 256;
    public static int beaconTime = 5000; // ms, 5s

    //folder names
    public static String InfoFolderName = "Info";
    public static String MessagesFolderName = "Messages";

    //file names
    public static String LastConversationsFileName = "LastConversations";
    public static String UserListFileName = "UserList";
    public static String OfflineMessagesFileName = "OfflineMessages";

    //voice chat enabled disabled
    public static boolean voiceChatSpeakDisabled = false;
    public static boolean voiceChatDisabled = false;

    // these should not be loaded from a config file, of course
    public InetAddress mGroup_;
    public static String hostInfo_;

    public Configuration() {
        InetAddress i;
        String s = "hostname";

        try {
            i = InetAddress.getLocalHost();
            s = i.getHostName();

            hostInfo_ = s;
        } catch (UnknownHostException e) {
            System.out.println("Problem: " + e.getMessage());
        }
    }
}
