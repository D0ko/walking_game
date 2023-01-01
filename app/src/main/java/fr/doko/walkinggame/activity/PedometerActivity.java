package fr.doko.walkinggame.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import java.text.MessageFormat;

import fr.doko.walkinggame.R;
import fr.doko.walkinggame.databinding.ActivityPedometerBinding;
import fr.doko.walkinggame.util.PedometerService;
import fr.doko.walkinggame.util.StepCallback;

public class PedometerActivity extends AppCompatActivity {
    ActivityPedometerBinding binding;
    boolean isRunning;
    Intent serviceIntent;
    private PedometerService pedometerService;
    public static final String TAG = "PedometerService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pedometer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
                Intent intent = new Intent(this, AuthorityActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                startService();
            }
        } else {
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isServiceRunningCheck() && serviceIntent != null) {
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isServiceRunningCheck()) {
            unbindService(serviceConnection);
        }
    }

    public void startService() {
        serviceIntent = new Intent(PedometerActivity.this, PedometerService.class);
        if (!isServiceRunningCheck()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                startService(serviceIntent);
            } else {
                startForegroundService(serviceIntent);
            }
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        int num = PedometerService.mSteps;
        binding.pedometerSteps.setText(MessageFormat.format("{0}", num));
        //binding.pedometerProgress.setProgress(num);
    }

    private StepCallback stepCallback = new StepCallback() {
        @Override
        public void onStepCallback(int step) {
            binding.pedometerSteps.setText(MessageFormat.format("{0}", step));
            //binding.pedometerProgress.setProgress(step);
        }

        @Override
        public void onUnbindService() {
            isRunning = false;
            Log.d(TAG, "unbind");
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PedometerService.PedometerBinder mb = (PedometerService.PedometerBinder) iBinder;
            pedometerService = mb.getService();
            pedometerService.setCallback(stepCallback);
            isRunning = true;
            Log.d(TAG, "serviceConnection connect");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isRunning = false;
            Log.d(TAG, "serviceConnection disconnect");
        }
    };

    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("fr.doko.walkinggame.util.PedometerService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
