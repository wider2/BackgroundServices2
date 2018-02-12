package rx.backgroundservices;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.backgroundservices.MainActivity;

public class SplashActivity extends AppCompatActivity {

    public static final int SplashTime = 1000;
    Thread splashTread;
    Animation anim;

    private static final String TAG = SplashActivity.class.getName();
    Bundle _savedInstanceState;
    private static final int RC_HANDLE_PERM = 255;

    @BindView(R.id.tv_output)
    TextView tvOutput;
    @BindView(R.id.iv_logo)
    ImageView ivLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in); //creating animation
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ivLogo.startAnimation(anim);


        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        checkPermission();
                        wait(SplashTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Log.wtf(TAG, "finally checked");
                }
            }
        };
        splashTread.start();
    }

    private void jumpActivity() {
        Intent i = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int rc = ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                jumpActivity();
            } else {
                requestStoragePermission();
            }
        } else {
            jumpActivity();
        }
    }


    private void requestStoragePermission() {
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_PERM);
            return;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RC_HANDLE_PERM);
        }

        final Activity thisActivity = this;

        final Snackbar snackbar = Snackbar.make(tvOutput, R.string.permission_storage_rationale, Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.word_ok), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_PERM);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission granted");
            checkPermission();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name))
                .setMessage(R.string.no_storage_permission)
                .setPositiveButton(R.string.word_ok, listener)
                .show();
    }

}