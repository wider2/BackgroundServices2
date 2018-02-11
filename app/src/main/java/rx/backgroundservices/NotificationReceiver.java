package rx.backgroundservices;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

    public static NotificationReceiverListener notificationReceiverListener;

    public NotificationReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        if (action.equals("actionName1")) {
            performAction1(context);
        } else if (action.equals("actionName2")) {
            performAction2(context);
        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void performAction1(Context context) {
        //Toast.makeText(context, "YES CALLED", Toast.LENGTH_SHORT).show();
        if (notificationReceiverListener != null) {
            notificationReceiverListener.onNotificationClicked(1);
        }
    }

    public void performAction2(Context context) {
        //Toast.makeText(context, "NO CALLED", Toast.LENGTH_SHORT).show();
        if (notificationReceiverListener != null) {
            notificationReceiverListener.onNotificationClicked(2);
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already", "running");
                return true;
            }
        }
        Log.i("Service not", "running");
        return false;
    }

    public interface NotificationReceiverListener {
        void onNotificationClicked(int value);
    }

}