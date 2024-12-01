package com.example.silenttimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TimerService extends Service {

    private CountDownTimer timerUtil;
    private BroadcastReceiver cancelTimerReceiver;
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("TIMER_DURATION")) {
            long timerDuration = intent.getLongExtra("TIMER_DURATION", 0);
            startTimer(timerDuration);
        }
        Notification notification = new NotificationCompat.Builder(this, "TIMER_SERVICE_CHANNEL")
                .setContentTitle("Timer Running")
                .setContentText("Your timer is active.")
                .setSmallIcon(R.drawable.ic_stat_name)
                .build();

        startForeground(1, notification);

        cancelTimerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("CANCEL_TIMER")) {
                    timerUtil.cancel();
                    stopSelf();
                    wakeLock.release();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(cancelTimerReceiver, new IntentFilter("CANCEL_TIMER"));

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "TIMER_SERVICE_CHANNEL",
                    "Timer Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the BroadcastReceiver when the service stops
        if (cancelTimerReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(cancelTimerReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Return null for an unbound service
    }

    private void startTimer(long duration) {
        timerUtil = new CountDownTimer(duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendUpdate(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // TODO: maybe better vibrate pattern
                v.vibrate(3000);
                Intent intent = new Intent("TIMER_FINISHED");
                LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(intent);
                stopSelf();
                wakeLock.release();

            }
        }.start();

        // make sure the cpu doesn't sleep when on lock screen
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TimerService:WakeLock");
        wakeLock.acquire(duration*1000);
    }

    private void sendUpdate(long remainingTime) {
        Intent updateIntent = new Intent("TIMER_UPDATE");
        updateIntent.putExtra("REMAINING_TIME", remainingTime);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }
}
