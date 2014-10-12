package tudelft.mdp.communication;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.fileManagement.FileCreator;

public class ListenerService extends WearableListenerService {


    private static final String LOGTAG = "MDP-WearableListenerService";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                if( event.getDataItem().getUri().getPath().equals(MessagesProtocol.FILEPATH)){

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String filename = dataMapItem.getDataMap().getString(MessagesProtocol.MESSAGE);
                    Asset file = dataMapItem.getDataMap().getAsset(MessagesProtocol.RECORDEDSENSORS);

                    Log.e(LOGTAG, "Asset received from wear: " + filename);
                    saveToFile(filename, file);

                } else {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v(LOGTAG, "DataMap received from watch: " + dataMap);
                    // Broadcast message to wearable activity for display
                    Intent messageIntent = new Intent(MessagesProtocol.WEARSENSORSBUNDLE);
                    messageIntent.putExtras(dataMap.toBundle());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(MessagesProtocol.MSGPATH)) {
            final String message = new String(messageEvent.getData());

            Log.i(LOGTAG, "Message received from watch: " + message);

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent(MessagesProtocol.WEARSENSORSMSG);
            messageIntent.putExtra(MessagesProtocol.MESSAGE, message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void saveToFile(String filename, Asset file){

        FileCreator mFileCreator = new FileCreator(filename, Constants.DIRECTORY_SENSORS);
        mFileCreator.openFileWriter();

        // read from byte array
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(file.getData());
            DataInputStream in = new DataInputStream(bais);
            while (in.available() > 0) {
                String element = in.readUTF();
                mFileCreator.saveData(element);
            }
        } catch (IOException e){
            Log.e(LOGTAG, e.getMessage());
        }

        mFileCreator.closeFileWriter();
        Toast.makeText(this, "File saved: " + mFileCreator.getPath(),
                Toast.LENGTH_SHORT).show();
    }

}
