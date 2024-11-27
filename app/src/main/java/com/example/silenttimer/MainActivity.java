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
    TextView countdown;
    EditText timerInput;
    Button startBtn;
    Button pauseBtn;
    Button resetBtn;
    CountDownTimer timerUtil;
    int startTime = 0;

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
        pauseBtn = findViewById(R.id.pause);
        pauseBtn.setOnClickListener(this::pauseCountdown);
        resetBtn = findViewById(R.id.reset);
        resetBtn.setOnClickListener(this::resetCountdown);
    }

    public void startCountdown(View view) {
        startBtn.setEnabled(false);
        startTime = Integer.parseInt(countdown.getText().toString());
        timerUtil = new CountDownTimer(startTime*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdown.setText(""+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);
                countdown.setText(""+startTime);

            }
        }.start();
        startBtn.setEnabled(true);
    }

    public void pauseCountdown(View view) {
        // TODO: implement pause utility and merge pause and start button
    }

    public void resetCountdown(View view) {
        if (timerUtil == null){
            return;
        }
        timerUtil.cancel();
        countdown.setText(""+startTime);
    }
}