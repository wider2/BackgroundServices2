package rx.backgroundservices.job;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class SyncJob extends Job {

    public static final String TAG = "job_tag";
    private static int jobId = -1;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {

        Toast.makeText(getContext(), "job123", Toast.LENGTH_SHORT).show();
        Log.wtf(TAG, "SyncJob is appear");

        scheduleJob();
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        final long INTERVAL = 900000L;
        final long FLEX = 300000L;
        jobId = new JobRequest.Builder(SyncJob.TAG)
                .setUpdateCurrent(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                //.setPeriodic(TimeUnit.MINUTES.toMillis(1))
                .setPeriodic(INTERVAL, FLEX)
                .build()
                .schedule();
    }

    public static void stop() {
        JobManager
                .instance()
                .cancel(jobId);
    }
}