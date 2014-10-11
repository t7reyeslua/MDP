package tudelft.mdp.communication;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import android.util.Log;

public class SendMessageThread extends Thread {
    String path;
    String message;


    GoogleApiClient mGoogleApiClient;
    private static final String LOGTAG = "MDP-SendMessageThread";

    // Constructor for sending data objects to the data layer
    public SendMessageThread(GoogleApiClient client, String p, String data) {
        path = p;
        message = data;
        mGoogleApiClient = client;
    }

    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {

            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.v(LOGTAG, "Message: {" + message + "} sent to: " + node.getDisplayName());
            }
            else {
                // Log an error
                Log.v(LOGTAG, "ERROR: failed to send Message");
            }
        }
    }
}