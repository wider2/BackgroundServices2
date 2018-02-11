package rx.backgroundservices.utils;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import rx.backgroundservices.R;


/**
 * Created by Aleksei Jegorov on 02/01/2018.
 */

public class Utilities {

    public static void writeFile(String filename, String output, boolean b, Context mContext, String catalog) {
        filename = filename.replace(" ", "-");
        File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        if (!root.exists()) root.mkdirs();

        try {
            //Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            //if (isSDPresent) {
            String state = Environment.getExternalStorageState();
            if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                if (!catalog.matches("")) catalog = "/" + catalog;
                root = new File(Environment.getExternalStorageDirectory(), mContext.getString(R.string.app_name) + "" + catalog);//Add folder
                if (!root.exists()) root.mkdirs();
                if (root.canWrite()) {
                    File gpxfile = new File(root, filename);
                    FileWriter writer = new FileWriter(gpxfile, b);
                    writer.append(output);
                    writer.flush();
                    writer.close();
                }
            } else {
                if (!catalog.matches("")) catalog = "" + catalog + "/";
                FileOutputStream fos = mContext.openFileOutput(catalog + filename, Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                osw.write(output);
                osw.flush();

                fos.flush();
                fos.getFD().sync();
                osw.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String GetTimeNow() {
        Time now = new Time();
        now.setToNow();

        Integer x = now.monthDay;
        String tnow = x.toString();
        x = now.month + 1;
        tnow += "/" + x.toString();
        x = now.year;
        tnow += "/" + x.toString();

        x = now.hour;
        tnow += " " + x.toString() + ":";
        x = now.minute;
        if (x < 10) tnow += "0";
        tnow += x.toString() + ":";
        x = now.second;
        if (x < 10) tnow += "0";
        tnow += x.toString();

        return tnow;
    }

    public static String truncate(String input, int limit) {
        return (input != null && input.length() > limit) ? (input.substring(0, limit) + "...") : input;
    }
}
