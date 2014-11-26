package tudelft.mdp.backend.gcs;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.logging.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;

public class GcsHelper {

    private static final Logger LOG = Logger.getLogger(GcsHelper.class.getName());
    public static final String BUCKETNAME = "tudelft-mdp.appspot.com";

    public GcsHelper() {
    }

    public AppEngineFile createObject(FileService fileService, String objectName){
        // Get the file service
        GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
                .setBucket(BUCKETNAME)
                .setKey(objectName)
                .setAcl("public-read");

        // Create your object
        AppEngineFile writableFile = null;
        try {
            writableFile = fileService.createNewGSFile(optionsBuilder.build());
        } catch (IOException e){
            LOG.severe(e.getMessage());
        }
        return writableFile;
    }

    public void writeWekaArffToGCS(String filename, Instances dataset){
        try {
            FileService fileService = FileServiceFactory.getFileService();
            AppEngineFile writableFile = createObject(fileService, filename);
            boolean lockForWrite = true;
            FileWriteChannel writeChannel = fileService.openWriteChannel(writableFile, lockForWrite);

            ArffSaver saver = new ArffSaver();
            saver.setInstances(dataset);
            saver.setDestination(Channels.newOutputStream(writeChannel));
            saver.writeBatch();

            // Finalize the object
            writeChannel.closeFinally();
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
    }

    public void writeWekaClsToGCS(String filename, Classifier cls){
        try {
            FileService fileService = FileServiceFactory.getFileService();
            AppEngineFile writableFile = createObject(fileService, filename);
            boolean lockForWrite = true;
            FileWriteChannel writeChannel = fileService.openWriteChannel(writableFile, lockForWrite);

            SerializationHelper.write(Channels.newOutputStream(writeChannel), cls);

            // Finalize the object
            writeChannel.closeFinally();
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
    }


    public void writeWekaInstanceToGCS(String filename, byte[] serializedWekaObject){
        try {
            FileService fileService = FileServiceFactory.getFileService();
            AppEngineFile writableFile = createObject(fileService, filename);
            boolean lockForWrite = true;
            FileWriteChannel writeChannel = fileService.openWriteChannel(writableFile, lockForWrite);

            writeChannel.write(ByteBuffer.wrap(serializedWekaObject));

            // Finalize the object
            writeChannel.closeFinally();
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
    }


    public void writeToGCS(String filename, ArrayList<String> text){
        try {
            LOG.info("No. lines in text: " + text.size());
            FileService fileService = FileServiceFactory.getFileService();
            AppEngineFile writableFile = createObject(fileService, filename);
            boolean lockForWrite = true;
            FileWriteChannel writeChannel = fileService.openWriteChannel(writableFile, lockForWrite);

            for (int i = 0; i< text.size(); i++) {
                String s  = text.get(i) + "\n";
                writeChannel.write(ByteBuffer.wrap(s.getBytes()));
            }

            // Finalize the object
            writeChannel.closeFinally();
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
    }

    public Classifier readClsFromGCS(String file){
        try {
            FileService fileService = FileServiceFactory.getFileService();
            String filename = "/gs/" + BUCKETNAME + "/" + file;
            AppEngineFile readableFile = new AppEngineFile(filename);
            FileReadChannel readChannel = fileService.openReadChannel(readableFile, false);
            // Again, different standard Java ways of reading from the channel.

            Classifier cls = (Classifier) SerializationHelper.read(Channels.newInputStream(readChannel));
            readChannel.close();

            //LOG.info(cls.toString());

            return cls;
        } catch (Exception e){
            LOG.severe(e.getMessage());
            return null;
        }
    }

    public Instances readInstancesFromGCS(String file){
        try {
            FileService fileService = FileServiceFactory.getFileService();
            String filename = "/gs/" + BUCKETNAME + "/" + file;
            AppEngineFile readableFile = new AppEngineFile(filename);
            FileReadChannel readChannel = fileService.openReadChannel(readableFile, false);
            // Again, different standard Java ways of reading from the channel.

            Instances data = (Instances) SerializationHelper.read(Channels.newInputStream(readChannel));
            //Instances data1 = new Instances(new BufferedReader(Channels.newReader(readChannel, "UTF8")));
            readChannel.close();

            //LOG.info(data.toSummaryString());
            //LOG.info(data1.toSummaryString());
            return data;
        } catch (Exception e){
            LOG.severe(e.getMessage());
            return null;
        }
    }



}
