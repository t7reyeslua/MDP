package tudelft.mdp.communication;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import tudelft.mdp.MainScreen;
import tudelft.mdp.R;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.services.SensorReaderService;


public class ListenerService extends WearableListenerService {

    private Intent mSensorReaderService;

    private int notificationId = 001;
    private static final String LOGTAG = "MDP-Wear WearableListenerService";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (MessagesProtocol.NOTIFICATIONPATH.equals(event.getDataItem().getUri().getPath())) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String title = dataMapItem.getDataMap().getString(MessagesProtocol.NOTIFICATIONTITLE);
                    String content = dataMapItem.getDataMap().getString(MessagesProtocol.NOTIFICATIONCONTENT);
                    String command = dataMapItem.getDataMap().getString(MessagesProtocol.NOTIFICATIONCOMMAND);

                    if (command.equals(MessagesProtocol.STARTSENSINGSERVICE)) {

                        Log.i(LOGTAG, "Sensing Service: START");
                        sendNotification(title, content);

                        if (!SensorReaderService.isRunning()) {
                            mSensorReaderService = new Intent(this, SensorReaderService.class);
                            this.startService(mSensorReaderService);
                        }

                    } else if (command.equals(MessagesProtocol.STOPSENSINGSERVICE)){

                        Log.i(LOGTAG, "Sensing Service: STOP");
                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                        notificationManagerCompat.cancel(notificationId);

                    }

                } else {
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v(LOGTAG, "DataMap received from mobile: " + dataMap);
                    // Broadcast message to wearable activity for display
                    Intent messageIntent = new Intent(MessagesProtocol.WEARSENSORSBUNDLE);
                    messageIntent.putExtras(dataMap.toBundle());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                }

            }
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
