package tudelft.mdp.communication;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import tudelft.mdp.MainScreen;
import tudelft.mdp.R;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.services.SensorReaderService;


public class ListenerService extends WearableListenerService {


    private int notificationId = 001;
    private static final String LOGTAG = "MDP-Wear WearableListenerService";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (MessagesProtocol.NOTIFICATIONPATH.equals(event.getDataItem().getUri().getPath())) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String title = dataMapItem.getDataMap().getString(MessagesProtocol.NOTIFICATIONTITLE);
                    String content = dataMapItem.getDataMap().getString(MessagesProtocol.NOTIFICATIONCONTENT);
                    String command = dataMapItem.getDataMap().getString(MessagesProtocol.NOTIFICATIONCOMMAND);

                    if (command.equals(MessagesProtocol.STARTSENSINGSERVICE)) {
                        sendNotification(title, content);

                        Log.i(LOGTAG, "Sensing Service: START");
                        Intent intent = new Intent(this, SensorReaderService.class);
                        this.startService(intent);
                    } else if (command.equals(MessagesProtocol.STOPSENSINGSERVICE)){
                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                        notificationManagerCompat.cancel(notificationId);

                        this.stopService(new Intent(this, SensorReaderService.class));
                    }


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

    private void sendNotification(String title, String content) {

        // this intent will open the activity when the user taps the "open" action on the notification
        Intent viewIntent = new Intent(this, MainScreen.class);
        PendingIntent pendingViewIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);


        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.plug)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingViewIntent)
                .extend(new Notification.WearableExtender().setBackground(bitmap));

        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, notification);

    }

}
