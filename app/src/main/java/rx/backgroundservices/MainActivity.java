package rx.backgroundservices;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.backgroundservices.job.SyncJob;
import rx.backgroundservices.services.DownloadService;
import rx.backgroundservices.services.MyService;

public class MainActivity extends AppCompatActivity
        implements NotificationReceiver.NotificationReceiverListener {

    private List<String> list;
    Snackbar snackBar;
    MyService mService;
    boolean mBound = false;
    int testValue = 0;

    @BindView(R.id.tv_output)
    TextView tvOutput;
    @BindView(R.id.iv_atom)
    ImageView ivAtom;
    @BindView(R.id.bt_service)
    Button btService;
    @BindView(R.id.bt_service2)
    Button btService2;


    @Override
    protected void onStart() {
        super.onStart();
        //set connection with service
        toggleMyService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        BgServicesApplication.get().setNotificationListener(this);

        SyncJob.scheduleJob();

        //Service for download image
        list = new ArrayList<>();
        list.add("https://www.carandclassic.co.uk/uploads/cars/bristol/8814918.jpg");
        list.add("https://www.carandclassic.co.uk/uploads/cars/bristol/8814920.jpg");
        list.add("https://www.carandclassic.co.uk/uploads/cars/bristol/8814922.jpg");
        list.add("https://www.carandclassic.co.uk/uploads/cars/bristol/8814924.jpg");
        list.add("https://www.carandclassic.co.uk/uploads/cars/bristol/8814926.jpg");
        list.add("https://www.carandclassic.co.uk/uploads/cars/bristol/8814928.jpg");

        String imageUrl = "";
        int min = 0, max = 5;
        Random r = new Random();
        int rand = r.nextInt(max - min + 1) + min;
        for (int i = 0; i < list.size(); i++) {
            if (i == rand) imageUrl = list.get(i);
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(receiverDownload, new IntentFilter("download_event"));
        Intent myDownloadIntentService = new Intent(this, DownloadService.class);
        startService(myDownloadIntentService
                .putExtra(DownloadService.URL, imageUrl)
                .putExtra(DownloadService.FILENAME, "image" + rand + ".jpg")
                .putExtra("task", "follow the white rabbit"));


        btService2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //not depend on activity restart
                if (MyService.isRunning) {
                    testValue = MyService.instance.doMethodOfService();
                    tvOutput.setText("Method of service return value: " + testValue);
                }
            }
        });

        btService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //another way to get method of Service
                if (mBound && mService != null) {
                    testValue = mService.testIBinderMethod();
                    tvOutput.setText("IBinder return value: " + testValue);
                }
            }
        });
    }


    public void toggleMyService() {
        if (!MyService.isRunning) {
            //Intent intent = new Intent(this, MyService.class);
            //stopService(intent);
        //} else {
            Intent intent = new Intent(this, MyService.class);
            intent.putExtra("key", "value");
            if (MyService.isPreAndroidO()) {
                startService(intent);
            } else {
                startForegroundService(intent);
            }
            if (!mBound) bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }


    private BroadcastReceiver receiverDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String filePath = bundle.getString(DownloadService.FILEPATH);
                int resultCode = bundle.getInt(DownloadService.RESULT);
                Log.wtf("DownloadService", "BootReceiver: " + filePath + "; " + resultCode);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Download complete. Download URI: " + filePath, Toast.LENGTH_LONG).show();
                    tvOutput.append("Download done");
                    ivAtom.setImageBitmap(BitmapFactory.decodeFile(filePath));
                } else {
                    Toast.makeText(MainActivity.this, "Download failed", Toast.LENGTH_LONG).show();
                    tvOutput.append("Download failed");
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Toast.makeText(getBaseContext(), getString(R.string.local_service_connected), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg) {
            mBound = false;
            Toast.makeText(getBaseContext(), getString(R.string.local_service_disconnected), Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onNotificationClicked(int param) {
        Log.d("Sender", "Broadcasting message received");
        showSnack(param);
    }

    private void showSnack(int param) {
        if (param == 1) {
            snackBar = Snackbar
                    .make(tvOutput, getString(R.string.notification_received) + param, Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(getResources().getColor(R.color.yellow))
                    .setAction(getString(R.string.network_settings), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });

            View sbView = snackBar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackBar.show();
        } else {
            if (snackBar != null) snackBar.dismiss();
        }
    }

}
