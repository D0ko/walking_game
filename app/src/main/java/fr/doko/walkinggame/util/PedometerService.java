package fr.doko.walkinggame.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import fr.doko.walkinggame.R;
import fr.doko.walkinggame.activity.PedometerActivity;

public class PedometerService extends Service implements SensorEventListener {
    public static int mSteps = 0;
    int previousStep = 0;
    private IBinder myBinder = new PedometerBinder();
    Intent intent;
    NotificationCompat.Builder builder;
    NotificationManager manager;
    PendingIntent pendingIntent;
    Sensor mCounterSensor;
    SensorManager mSensorManager;
    private StepCallback callback;
    public static final String TAG = "PedometerService123";
    String title, text;

    public class PedometerBinder extends Binder {
        public PedometerService getService() {
            return PedometerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public void setCallback(StepCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initStepSensor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setForegroundNotification();

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (previousStep < 1) {
                previousStep = (int) sensorEvent.values[0];
            }
            mSteps = (int) sensorEvent.values[0] - previousStep;
            if (builder != null) {
                builder.setContentText("PAS: " + mSteps  + " !");
                manager.notify(1000, builder.build());
            }
            if (callback != null) {
                callback.onStepCallback(mSteps);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void initStepSensor() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (mCounterSensor == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_sensor_not_found), Toast.LENGTH_SHORT).show();
        } else {
            mSensorManager.registerListener(this, mCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }


    public void setForegroundNotification() {
        Intent intent = new Intent(getApplicationContext(), PedometerActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("service", "Service", NotificationManager.IMPORTANCE_MIN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(false);

            manager.createNotificationChannel(channel);
            title = getResources().getString(R.string.app_name);
            text = getResources().getString(R.string.notification_default);
            builder = new NotificationCompat.Builder(this, channel.getId())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setChannelId("service")
                    .setShowWhen(false)
                    .setContentIntent(pendingIntent);
        } else {
            title = getResources().getString(R.string.app_name);
            text = getResources().getString(R.string.notification_default);
            builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(-2)
                    .setShowWhen(false)
                    .setContentIntent(pendingIntent);
        }
        startForeground(1000, builder.build());
    }

    public void unRegisterManager() {
        try {
            mSensorManager.unregisterListener(this);
            mSteps = 0;
            previousStep = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterManager();
        stopForeground(true);
    }
}
