package tudelft.mdp.communication;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import android.util.Log;

import java.util.ArrayList;

import tudelft.mdp.enums.MessagesProtocol;

public class SendFileByMessagesThread extends Thread {
    String path;
    ArrayList<String> messageList;


    GoogleApiClient mGoogleApiClient;
    private static final String LOGTAG = "MDP-SendMessageThread";

    // Constructor for sending data objects to the data layer
    public SendFileByMessagesThread(GoogleApiClient client, String p, ArrayList<String> data) {
        path = p;
        messageList = data;
        mGoogleApiClient = client;
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
                Log.w(LOGTAG,"Start sending file from thread");
                String startMsg = MessagesProtocol.SENDSENSEORSNAPSHOTREC_START + "| Start saving file";
                sendMsg(startMsg, node);


                Log.w(LOGTAG,"Number of records to send: " + messageList.size());
                for (String message : messageList) {
                    String record = MessagesProtocol.SENDSENSEORSNAPSHOTREC + "|" + message;
                    sendMsg(record, node);
                }


                Log.w(LOGTAG,"Stop sending file from thread");
                String finishMsg = MessagesProtocol.SENDSENSEORSNAPSHOTREC_FINISH + "| Finish saving file";
                sendMsg(finishMsg, node);
            }
        }
    }
}