package com.example.silenttimer;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private CountDownTimer timerUtil;
    private int startTime = 0;
    private long millisLeft = 0;

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
    }

    public void startCountdown(View view) {
        if (countdownStatus != Status.RESET) { return; }
        try {
            startTime = Integer.parseInt(countdown.getText().toString());
        } catch (NumberFormatException e) { return; }
        timerUtil = new CountDownTimer((long) startTime*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdown.setText(""+millisUntilFinished/1000);
                millisLeft = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(3000);
                countdown.setText(""+startTime);
                startBtn.setOnClickListener(MainActivity.this::startCountdown);
                startBtn.setText("Start");
                countdownStatus = Status.RESET;

            }
        }.start();
        startBtn.setOnClickListener(this::pauseCountdown);
        startBtn.setText("Pause");
        countdownStatus = Status.RUNNING;
    }

    public void pauseCountdown(View view) {
        if (countdownStatus != Status.RUNNING) { return; }
        timerUtil.cancel();
        startBtn.setOnClickListener(this::resumeCountdown);
        startBtn.setText("Resume");
        countdownStatus = Status.PAUSED;
    }

    public void resumeCountdown(View view) {
        if (countdownStatus != Status.PAUSED) { return; }
        timerUtil = new CountDownTimer(millisLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdown.setText(""+millisUntilFinished/1000);
                millisLeft = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(3000);
                countdown.setText(""+startTime);
                startBtn.setOnClickListener(MainActivity.this::startCountdown);
                startBtn.setText("Start");
                countdownStatus = Status.RESET;

            }
        }.start();
        startBtn.setOnClickListener(this::pauseCountdown);
        startBtn.setText("Pause");
        countdownStatus = Status.RUNNING;
    }

    public void resetCountdown(View view) {
        if (countdownStatus == Status.RUNNING) {
            timerUtil.cancel();
        }
        countdown.setText(""+startTime);
        startBtn.setOnClickListener(this::startCountdown);
        startBtn.setText("Start");
        countdownStatus = Status.RESET;
    }
}