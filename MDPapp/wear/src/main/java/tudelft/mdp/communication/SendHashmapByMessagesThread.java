package tudelft.mdp.communication;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import tudelft.mdp.Utils;
import tudelft.mdp.enums.MessagesProtocol;

/**
 * Created by t7 on 31-10-14.
 */
public class SendHashmapByMessagesThread extends Thread {
    String path;
    HashMap<Integer, ArrayList<String>> mRecordedSensors;


    GoogleApiClient mGoogleApiClient;
    private static final String LOGTAG = "MDP-SendHashmapByMessagesThread";

    // Constructor for sending data objects to the data layer
    public SendHashmapByMessagesThread(GoogleApiClient client,
            String p,
            HashMap<Integer, ArrayList<String>> hmSensors) {
        path = p;
        mGoogleApiClient = client;
        mRecordedSensors = new HashMap<Integer, ArrayList<String>>(hmSensors);
    }

    private void  sendMsg(String message, Node node){
        MessageApi.SendMessageResult result = Wearable.MessageApi
                .sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes())
                .await();
        if (result.getStatus().isSuccess()) {
            Log.v(LOGTAG,
                    "Message: {" + message + "} sent to: " + node.getDisplayName());
        } else {
            // Log an error
            Log.v(LOGTAG, "ERROR: failed to send Message");
        }
    }


    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        if (nodes.getNodes() != null) {
            for (Node node : nodes.getNodes()) {

                Log.w(LOGTAG, "Info - Number of sensors to send: " + mRecordedSensors.size());
                for (Integer sensorType : mRecordedSensors.keySet()) {
                    ArrayList<String> messageList = mRecordedSensors.get(sensorType);
                    Log.w(LOGTAG, "Info - Start sending file from thread: " + Utils.getSensorName(sensorType));
                    String startMsg = MessagesProtocol.SENDSENSEORSNAPSHOTREC_START
                            + "|" + sensorType;
                    sendMsg(startMsg, node);


                    /*
                    Log.w(LOGTAG, "Sending: " + Utils.getSensorName(sensorType));
                    String header = MessagesProtocol.SENDSENSEORSNAPSHOTHEADER + "|"
                            + buildHeader(sensorType);
                    sendMsg(header, node);*/

                    Log.w(LOGTAG, "Info - Number of records to send: " + messageList.size());
                    for (String message : messageList) {
                        String record = MessagesProtocol.SENDSENSEORSNAPSHOTREC + "|" + message;
                        sendMsg(record, node);
                    }

                    Log.w(LOGTAG, "Info - Stop sending file from thread");
                    String finishMsg = MessagesProtocol.SENDSENSEORSNAPSHOTREC_FINISH
                            + "|" + sensorType;
                    sendMsg(finishMsg, node);
                }


                Log.w(LOGTAG,"Info - Stop transmission from thread");
                String endMsg = MessagesProtocol.SENDSENSEORSNAPSHOT_END +  "|" + " END TRANSMISSION";
                sendMsg(endMsg, node);
            }
        }
    }

    private String buildHeader(int sensorType){
        String header = "No.\tTimestamp\t";
        header += " [" + Utils.getSensorLength(sensorType) + "]" + Utils.getSensorName(sensorType) + "\t";
        return header;
    }
}
