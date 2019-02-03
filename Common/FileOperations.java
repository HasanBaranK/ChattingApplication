package Common;

import java.io.*;
import java.util.ArrayList;

/**
 * This is a library I have created for File operations
 * It is capable of: reading,writing, deleting ,getting, Overwriting
 *
 * */

public class FileOperations {

    /**
     * Writes to a file
     *
     * @param FolderName folder name
     * @param fileName file name
     * @param text The text you want to write
     * */
    public static void writeFile(String fileName, String FolderName, String text) {

        //Create the folder not exist
        boolean success = (new File(FolderName)).mkdir();

        File file = new File(FolderName + File.separator + fileName + ".txt");
        try {
            //create file if does not exist
            if (!file.exists()) {
                System.out.println("Create a new file.");
                file.createNewFile();
            }
            //write at the end of the file
            PrintWriter out = new PrintWriter(new FileWriter(file, true));
            out.println(text);
            out.close();
        } catch (IOException e) {
            System.out.println("COULD NOT WRITE!!");
        }

    }

    public static void writeFileOverride(String fileName, String FolderName, String text) {
        //Create folder and file name
        //Create the folder if not exist
        boolean success = (new File(FolderName)).mkdir();

        File file = new File(FolderName + File.separator + fileName + ".txt");
        ArrayList<String> lines = new ArrayList<>();
        try {
            //create file if does not exist
            if (!file.exists()) {
                System.out.println("Create a new file.");
                file.createNewFile();
            }
            BufferedReader reader;

            reader = new BufferedReader(new FileReader(
                    FolderName + File.separator + fileName + ".txt"));
            String line = reader.readLine();
            //read all lines and add it to a array list
            while (line != null) {

                lines.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {

        }
        //turn array list to an array
        String[] lineArray = lines.toArray(new String[lines.size()]);

        try {
            PrintWriter out = new PrintWriter(file);
            boolean userUpdated = false;
            String[] splittedline = text.split(",");
            //write the lines one by one while checking if something changed
            for (String line : lineArray) {
                String[] lineSplit = line.split(",");
                if (lineSplit.length > 2) {
                    if (lineSplit[1].equals(splittedline[1])) {
                        userUpdated = true;
                        out.println(text);
                    } else {
                        out.println(line);
                    }
                }
            }
            //If there is no anything new don`t change anything
            if (!userUpdated) {
                out.println(text);
            }

            out.close();
        } catch (IOException e) {
            System.out.println("COULD NOT WRITE!!");
        }

    }

    /**
     * Reads the file and return each line in a String array
     *
     * @param fileName file name
     * @param folderName  folder name
     *
     * @return String[] lines of the file
     * */
    public static String[] readFile(String folderName, String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    folderName + File.separator + fileName + ".txt"));
            String line = reader.readLine();
            while (line != null) {

                lines.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            //file is empty or does not exist
        }
        String[] file = lines.stream().toArray(String[]::new);
        //String[] file =  (String[]) lines.toArray("\n");
        return file;
    }
    /**
     * get the individual user from the user List File
     * */

    public static String[] get(String userId) {
        String[] lines = readFile(Configuration.InfoFolderName, Configuration.UserListFileName);
        //check the lines
        for (String line : lines) {
            String[] splittedline = line.split(",");
            if (splittedline.length > 4) {
                //if it is same return
                if (splittedline[1].trim().equals(userId.trim())) {
                    return splittedline;
                }

            }
        }
        //if user does not exist return null
        return null;
    }
    /**
     * Deletes line from the Offline messages
     * This method limits offline messages with one line
     * */
    public static void deleteLine(String folderName, String fileName, String deletedLine) {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    folderName + File.separator + fileName + ".txt"));
            String line = reader.readLine();
            while (line != null) {

                if (line.equals(deletedLine)) {

                } else {
                    lines.add(line);
                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {

        }

        File file = new File(folderName + File.separator + fileName + ".txt");
        String[] lineArray = lines.toArray(new String[lines.size()]);

        try {
            PrintWriter out = new PrintWriter(file);
            for (String line : lineArray) {
                out.println(line);
            }
            out.close();
        } catch (IOException e) {
            System.out.println("COULD NOT WRITE!!");
        }
    }
}
