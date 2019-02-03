package Common;

import Common.Configuration;
import Common.FileOperations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserList {

    //HashMap containing users
    static HashMap<String, String[]> userList = new HashMap<>();

    /**
     * updates the user list and hash map every time it gets a beacon
     * Although I could update the userList every 5 seconds in a different thread this was also doing it job and it wasn't vulnerable against floods
     * */
    public void updateUserList(String userBeacon) {

        //splits the beacon in to an array list
        String[] beaconSplitted = userBeacon.split("]");
        if (beaconSplitted.length == 5) {

            beaconSplitted[0] = beaconSplitted[0].replace("[", "");
            beaconSplitted[1] = beaconSplitted[1].replace("[", "");
            beaconSplitted[1] = beaconSplitted[1].trim();
            beaconSplitted[2] = beaconSplitted[2].replace("[", "");
            beaconSplitted[3] = beaconSplitted[3].replace("[", "");
            beaconSplitted[4] = beaconSplitted[4].replace("[", "");

            //puts the users in a hash map
            userList.put(beaconSplitted[1], beaconSplitted);

            //updates the userList.txt file using HashMap
            updateUserList();
            //updates the HashMap using using user list and equalize them - Not necessary but I wanted to make them equal-
            updateHashMap();
        }

    }
    /**
     * Updates the Hash map using the file
     *
     * */
    private void updateHashMap() {

        userList.clear();
        for (String line : FileOperations.readFile(Configuration.InfoFolderName, Configuration.UserListFileName)) {
            String[] splittedLine = line.split("]");
            if (splittedLine.length == 5) {
                userList.put(splittedLine[1], splittedLine);
                updateUserList();
                checkStatus(splittedLine[1]);
            }

        }


    }
    /**
     * updates the user list using hash map
     *
     * */
    public void updateUserList() {
        String text = "";
        for (String[] value : userList.values()) {
            text = text + value[0] + "," + value[1] + "," + value[2].toLowerCase() + "," + value[3] + "," + value[4] + "\n";
        }
        FileOperations.writeFileOverride(Configuration.UserListFileName, Configuration.InfoFolderName, text);
    }
    /**
     * Gets the port of the given user
     *
     * @param userId the user that we want its port
     * */
    public static String getPort(String userId) {

        String[] userInfo = FileOperations.get(userId);
        if (userInfo == null || userInfo.length < 2) {
            return "UserNotRegistered";
        }
        return userInfo[4];


    }
    /**
     * Gets the ip of the given user
     *
     * @param userId the user that we want its ip
     * */
    public static String getIp(String userId) {

        String[] userInfo = FileOperations.get(userId.trim());
        if (userInfo == null || userInfo.length < 2) {
            return "UserNotRegistered";
        }
        return userInfo[3];

    }
    /**
     * Gets the last seen of the given user
     *
     * @param userId the user that we want its last seen
     * */
    public static String getLastSeen(String userId) {
        String[] userInfo = FileOperations.get(userId);
        if (userInfo == null || userInfo.length < 2) {
            return "UserNotRegistered";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        try {
            Date lastSeen = sdf.parse(userInfo[0]);

            return lastSeen.toString();
        } catch (ParseException e) {
            //parse error
        }
        return "";
    }
    /**
     * Checks the status of the given user
     *
     * @param userName the user that we want its status
     * */
    public static String checkStatus(String userName) {
        String[] userInfo = FileOperations.get(userName);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        try {
            Date lastSeen = sdf.parse(userInfo[0]);
            Date date = new Date();
            long diffInMillies = Math.abs(date.getTime() - lastSeen.getTime());
            if (diffInMillies < 8000) {

                return userInfo[2];
            } else {
                return "offline";

            }


        } catch (Exception e) {
            return "offline";
        }

    }


}
