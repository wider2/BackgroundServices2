package rx.backgroundservices.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import rx.backgroundservices.MainActivity;
import rx.backgroundservices.R;


public class MyService extends Service {

    private final static String TAG = MyService.class.toString();
    private int NOTIFICATION = 1;
    public static final String CHANNEL_ID = String.valueOf(getRandomNumber());

    public static boolean isRunning = false;
    public static MyService instance = null;
    private NotificationManager notificationManager = null;


    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MyService getService() {
            return MyService.this; // Return this instance of LocalService so clients can call public methods
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int testIBinderMethod() {
        Random r = new Random();
        return r.nextInt(1001);
    }


    @Override
    public void onCreate() {
        instance = this;
        isRunning = true;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String value = intent.getStringExtra("key");
        Uri soundPath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (isPreAndroidO()) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

            Intent intentAction = new Intent();
            intentAction.putExtra("action", "actionName1");
            intentAction.setAction("actionName1");
            PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 1001, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent intentAction2 = new Intent();
            intentAction2.putExtra("action", "actionName2");
            intentAction2.setAction("actionName2");
            PendingIntent pendingIntentNo = PendingIntent.getBroadcast(this, 1001, intentAction2, PendingIntent.FLAG_UPDATE_CURRENT);


            // Set the info for the views that show in the notification panel.
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_flare)
                    .setTicker(getString(R.string.notification_text_status))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(getString(R.string.notification_text_title) + " " + value)
                    .setContentText(getString(R.string.notification_text_content))
                    .setContentIntent(contentIntent)
                    .addAction(R.drawable.hand_yes, getBaseContext().getString(R.string.word_yes), pendingIntentYes)
                    .addAction((R.drawable.hand_no), getBaseContext().getString(R.string.word_no), pendingIntentNo)
                    .setOngoing(true)       //make persistent: disable swipe-away
                    .setSound(soundPath)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(NOTIFICATION, notification);

            startForeground(NOTIFICATION, notification);

        } else {
            String channelId = createChannelOreo(getBaseContext());
            Notification notification = buildNotificationOreo(getBaseContext(), channelId);
            startForeground(NOTIFICATION, notification);
        }

        return START_STICKY;
    }


    @NonNull
    private String createChannelOreo(Context ctx) {

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence channelName = "Playback channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        notificationManager.createNotificationChannel(notificationChannel);
        return CHANNEL_ID;
    }

    private Notification buildNotificationOreo(Context context, String channelId) {
        /*
        // Action to stop the service.
        Notification.Action stopAction =
                new Notification.Action.Builder(
                        STOP_ACTION_ICON,
                        getNotificationStopActionText(context),
                        piStopService)
                        .build();
        */
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        return new Notification.Builder(context, channelId)
                .setContentTitle(getString(R.string.notification_text_title))
                .setContentText(getString(R.string.notification_text_content))
                .setSmallIcon(R.drawable.ic_stat_flare)
                .setContentIntent(contentIntent)
                //.setActions(stopAction)
                .setStyle(new Notification.BigTextStyle())
                .build();
    }


    public static boolean isPreAndroidO() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        instance = null;
        notificationManager.cancel(NOTIFICATION);
        super.onDestroy();
    }

    public int doMethodOfService() {
        Toast.makeText(getApplicationContext(), "Doing stuff from service...", Toast.LENGTH_SHORT).show();
        return new Random().nextInt(1001);
    }

    public static int getRandomNumber() {
        Random r = new Random();
        return r.nextInt(999);
    }

}
