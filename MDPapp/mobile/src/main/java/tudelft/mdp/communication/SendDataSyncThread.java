package tudelft.mdp.communication;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.util.Log;

public class SendDataSyncThread extends Thread {
    String path;
    DataMap dataMap;

    GoogleApiClient mGoogleApiClient;
    private static final String LOGTAG = "MDP-SendDataSyncThread";

    // Constructor for sending data objects to the data layer
    public SendDataSyncThread(GoogleApiClient client, String p, DataMap data) {
        path = p;
        dataMap = data;
        mGoogleApiClient = client;
    }

    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {

            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient,request).await();
            if (result.getStatus().isSuccess()) {
                Log.v(LOGTAG, "DataMap: " + dataMap + " sent to: " + node.getDisplayName());
            } else {
                // Log an error
                Log.v(LOGTAG, "ERROR: failed to send DataMap");
            }
        }
    }
}
