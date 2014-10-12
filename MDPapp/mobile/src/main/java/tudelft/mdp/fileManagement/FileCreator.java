package tudelft.mdp.fileManagement;


import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import tudelft.mdp.enums.Constants;

/**
 * File creator class
 */
public class FileCreator {

    private String filename;
    private String foldername;
    private File outputFile;
    private File inputFile;
    private FileInputStream fstream;
    private DataInputStream inputStream;

    BufferedReader brInputFile = null;
    BufferedWriter bwOutputFile = null;

    private final static String LOGTAG = "FileCreator";

    /**
     * Constructor
     * @param filename
     * @param foldername
     */
    public FileCreator(String filename, String foldername){
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        this.filename = "log_" + filename + "_" +date + ".txt" ;
        this.foldername = foldername;
    }

    public FileCreator(String filename, String foldername, boolean backup){
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        this.filename = "backup_" + filename + "_" +date + ".txt" ;
        this.foldername = foldername;
    }

    /**
     * Closes the writer stream
     */
    public void closeFileWriter(){
        if (bwOutputFile!=null) {
            try {
                bwOutputFile.close();
                Log.i(LOGTAG, "File saved");
            } catch (IOException e) {
                Log.e(LOGTAG, "Unable to close the logging file");
            }
        }

        if(outputFile.length() == 0){
            try {
                outputFile.delete();
            } catch  (SecurityException e){
                Log.e(LOGTAG, "Unable to delete the file");
            }
        }

        bwOutputFile = null;
    }

    /**
     * Opens the writer stream
     */
    public void openFileWriter(){
        File directory = getLoggingDirectory(foldername);
        if (null == directory) {
            return;
        }
        // Open file
        outputFile = new File(directory, filename);
        try {
            FileWriter fwOutputFile = new FileWriter(outputFile, true);
            bwOutputFile = new BufferedWriter(fwOutputFile);

        } catch  (IOException e){
            Log.e(LOGTAG, "Unable to create Buffered writer");
        }

    }

    /**
     * Closes the reader stream
     */
    public void closeFileReader(){
        if (inputStream !=null) {
            try {
                inputStream.close();
                Log.i(LOGTAG, "File closed");

            } catch (IOException e) {
                Log.e(LOGTAG, "Unable to close the file");
            }
        }

    }

    /**
     * Open the writer stream
     */
    public void openFileReader(String filename){
        this.filename = filename;
        File directory = FileCreator.getLoggingDirectory(foldername);
        if (null == directory) {
            Log.i(LOGTAG, "Directory null");
            return;
        }

        Log.i(LOGTAG, "Starting to read Info File from " + directory.getAbsolutePath());
        inputFile = new File(directory, filename);
        Log.i(LOGTAG, "inPutFile: " + inputFile.getAbsolutePath());

        try {
            fstream = new FileInputStream(inputFile);

        } catch  (FileNotFoundException e){
            Log.e(LOGTAG, "Unable to open file to read ");
        }

        // Get the object of DataInputStream
        inputStream = new DataInputStream(fstream);
        Log.i(LOGTAG, "DataInputStream");

        brInputFile = new BufferedReader(new InputStreamReader(inputStream));
        Log.i(LOGTAG, "BufferedReader");

    }

    /**
     * Determines the directory where the file is stored
     * @return directory
     */
    public static File getLoggingDirectory(String foldername) {
        File directory;
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(LOGTAG, "No external storage found.");
            return null;
        }
        directory = new File(Environment.getExternalStoragePublicDirectory(Constants.DIRECTORY_APP) , foldername);
        if (!directory.mkdirs()) {
            Log.v(LOGTAG, "Directory already exists");
        }
        return directory;
    }

    /**
     * Saves the data into the opened filewriter
     * @param data input string to be stored
     */
    public void saveData(String data){
        try {
            bwOutputFile.write(data);
            bwOutputFile.flush();
        } catch (IOException e ) {
            Log.e(LOGTAG, "Error while writing data to logging file.");
        }
    }

    /**
     * Reads the next line from the file
     * @return A line from the file
     */
    public String readData(){
        String line = null;
        try {
            line = brInputFile.readLine();
        } catch (IOException e) {
            Log.e(LOGTAG, "Unable to read line");
        }
        return line;
    }

    /**
     * getPath
     * @return a String with the path of the outputFile
     */
    public String getPath(){
        return outputFile.getPath();
    }

}
