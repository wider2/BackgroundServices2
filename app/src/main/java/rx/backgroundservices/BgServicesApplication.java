package rx.backgroundservices;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.JobManager;

import rx.backgroundservices.job.MyJobCreator;
import rx.backgroundservices.utils.Utilities;


public class BgServicesApplication extends Application {

    private final static String TAG = BgServicesApplication.class.toString();
    public static Thread.UncaughtExceptionHandler androidDefaultUEH;
    @NonNull
    private static BgServicesApplication instance;

    public BgServicesApplication() {
        instance = this;
    }

    @NonNull
    public static BgServicesApplication get() {
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.v("VircaApplication", "finalize is call");
        super.finalize();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            //setupFabric();

            JobManager.create(this).addJobCreator(new MyJobCreator());

        } catch (Exception e) {
            e.printStackTrace();
        }


        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                String report = "";
                StackTraceElement[] arr = paramThrowable.getStackTrace();
                report = paramThrowable.toString() + "\r\n";
                report += "--------- Stack trace ---------\r\n" + paramThread.toString();
                for (int i = 0; i < arr.length; i++) {
                    report += "    " + arr[i].toString() + "\r\n";
                }

                Throwable cause = paramThrowable.getCause();
                if (cause != null) {
                    report += "\n------------ Cause ------------\r\n";
                    report += cause.toString() + "\r\n";
                    arr = cause.getStackTrace();
                    for (int i = 0; i < arr.length; i++) {
                        report += "    " + arr[i].toString() + "\r\n";
                    }
                }

                String rep = "";
                rep += "Time: " + Utilities.GetTimeNow() + "\r\n";
                rep += "Message: " + paramThrowable.getMessage() + "\r\n";

                Utilities.writeFile("CrashReport.txt", rep + report + "\r\n", false, getApplicationContext(), "");

                androidDefaultUEH.uncaughtException(paramThread, paramThrowable);
            }
        });
    }

    private void setupFabric() {
        /*
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret));

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                //  .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .core(new CrashlyticsCore.Builder().disabled(false).build())
                .build();

        Fabric.with(getApplicationContext(), crashlyticsKit, new Twitter(authConfig));
        Crashlytics.setString("BuildType", BuildConfig.BUILD_TYPE);
        */
    }

    public void setNotificationListener(NotificationReceiver.NotificationReceiverListener listener) {
        NotificationReceiver.notificationReceiverListener = listener;
    }

}
