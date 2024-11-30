package com.example.silenttimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    enum Status {
        RESET,
        RUNNING,
        PAUSED
    }
    Status countdownStatus = Status.RESET;
    private TextView countdown;
    private EditText timerInput;
    private Button startBtn;
    private Button resetBtn;
    private long startTime = 0;
    private long millisLeft = 0;
    private BroadcastReceiver timerUpdateReceiver;
    private BroadcastReceiver timerFinishedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        countdown = findViewById(R.id.countdown);
        timerInput = findViewById(R.id.timerInput);
        timerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                countdown.setText(timerInput.getText().toString());
            }
        });
        startBtn = findViewById(R.id.start);
        startBtn.setOnClickListener(this::startCountdown);
        resetBtn = findViewById(R.id.reset);
        resetBtn.setOnClickListener(this::resetCountdown);

        timerUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals("TIMER_UPDATE")) {
                    long remainingTime = intent.getLongExtra("REMAINING_TIME", 0);
                    countdown.setText("" + remainingTime / 1000);
                    millisLeft = remainingTime;
                }
            }
        };

        timerFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("TIMER_FINISHED")) {
                    countdown.setText("" + startTime);
                    startBtn.setOnClickListener(MainActivity.this::startCountdown);
                    startBtn.setText("Start");
                    countdownStatus = MainActivity.Status.RESET;
                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(timerUpdateReceiver);
                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(timerFinishedReceiver);
                }
            }
        };
    }

    public void startCountdown(View view) {
        if (countdownStatus != Status.RESET) { return; }
        try {
            startTime = Long.parseLong(countdown.getText().toString());
        } catch (NumberFormatException e) { return; }
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, new IntentFilter("TIMER_UPDATE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(timerFinishedReceiver, new IntentFilter("TIMER_FINISHED"));
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra("TIMER_DURATION", startTime);
        ContextCompat.startForegroundService(this, intent);
        startBtn.setOnClickListener(this::pauseCountdown);
        startBtn.setText("Pause");
        countdownStatus = Status.RUNNING;
    }

    public void pauseCountdown(View view) {
        if (countdownStatus != Status.RUNNING) { return; }
        Intent intent = new Intent("CANCEL_TIMER");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        startBtn.setOnClickListener(this::resumeCountdown);
        startBtn.setText("Resume");
        countdownStatus = Status.PAUSED;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerFinishedReceiver);
    }

    public void resumeCountdown(View view) {
        if (countdownStatus != Status.PAUSED) { return; }
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, new IntentFilter("TIMER_UPDATE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(timerFinishedReceiver, new IntentFilter("TIMER_FINISHED"));
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra("TIMER_DURATION", millisLeft/1000);
        ContextCompat.startForegroundService(this, intent);
        startBtn.setOnClickListener(this::pauseCountdown);
        startBtn.setText("Pause");
        countdownStatus = Status.RUNNING;
    }

    public void resetCountdown(View view) {
        if (countdownStatus == Status.RUNNING) {
            Intent intent = new Intent("CANCEL_TIMER");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(timerUpdateReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(timerFinishedReceiver);
        }
        countdown.setText(""+startTime);
        startBtn.setOnClickListener(this::startCountdown);
        startBtn.setText("Start");
        countdownStatus = Status.RESET;
    }
}