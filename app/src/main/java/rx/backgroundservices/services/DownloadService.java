package rx.backgroundservices.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import rx.backgroundservices.R;

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String TAG = DownloadService.class.getName();
    public static final String URL = "url";
    public static final String FILENAME = "image.jpg";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean out;
        String urlPath = intent.getStringExtra(URL);
        String fileName = intent.getStringExtra(FILENAME);
        //File root = new File(Environment.getExternalStorageDirectory() + "/" + getBaseContext().getString(R.string.app_name) + "/", fileName);
        File root = new File(Environment.getExternalStorageDirectory(), fileName);
        //File root = new File(Environment.DIRECTORY_DOWNLOADS, fileName);

        InputStream stream = null;
        FileOutputStream fos = null;
        try {
            String state = Environment.getExternalStorageState();
            if (state.equals(Environment.MEDIA_MOUNTED)) {
                if (root.exists()) {
                    out = root.delete();
                    Log.wtf(TAG, "File deleted: " + out);
                }
                out = root.createNewFile();
                Log.wtf(TAG, "File created: " + out);
            } else {
                out = isInternalFilePresent(fileName);
                Log.wtf(TAG, "File deleted: " + out);

                File resolveMeSDCard = new File(root.getAbsolutePath());
                out = resolveMeSDCard.createNewFile();
                Log.wtf(TAG, "File created: " + out);
            }

            URL url = new URL(urlPath);
            //stream = url.openConnection().getInputStream();
            //InputStreamReader reader = new InputStreamReader(stream);

            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();


            InputStream in = new BufferedInputStream(url.openStream(), 8192);
            int total = 0;
            OutputStream outStream = new BufferedOutputStream(new FileOutputStream(root, true));
            byte data[] = new byte[1024];
            for (int b; (b = in.read(data)) != -1; ) {
                total += b / 1000;
                outStream.write(data, 0, b);
            }

            result = Activity.RESULT_OK;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        publishResult(root.getAbsolutePath(), result);
    }

    public boolean isInternalFilePresent(String fileName) {
        String path = getBaseContext().getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

    private void publishResult(String outputPath, int result) {
        Log.wtf("DownloadService", outputPath + "; " + result);

        //local broadcast accessible only to this application
        Intent intent = new Intent("download_event");
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}