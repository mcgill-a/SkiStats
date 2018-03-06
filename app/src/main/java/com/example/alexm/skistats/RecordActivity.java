package com.example.alexm.skistats;

import android.icu.text.AlphabeticIndex;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class RecordActivity extends AppCompatActivity {


    private ImageButton recordImageButton, pauseImageButton, cancelImageButton, submitImageButton;
    private TextView stopwatch;
    long millisecondTime, startTime, timeBuff, updateTime = 0L;
    int seconds, minutes, milliseconds;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        recordImageButton = (ImageButton)findViewById(R.id.recordImageButton);
        pauseImageButton = (ImageButton)findViewById(R.id.pauseImageButton);
        cancelImageButton = (ImageButton)findViewById(R.id.cancelImageButton);
        submitImageButton = (ImageButton)findViewById(R.id.submitImageButton);
        stopwatch = (TextView)findViewById(R.id.stopwatch);

        handler = new Handler();

        recordImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                startTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);

                Toast.makeText(RecordActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();

            }
        });

        pauseImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                timeBuff += millisecondTime;

                handler.removeCallbacks(runnable);

                Toast.makeText(RecordActivity.this, "Paused Recording", Toast.LENGTH_SHORT).show();
            }
        });

        cancelImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(RecordActivity.this, "Cancelled Recording", Toast.LENGTH_SHORT).show();
            }
        });

        submitImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(RecordActivity.this, "Submitted Recording", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public Runnable runnable = new Runnable() {

        public void run() {

            millisecondTime = SystemClock.uptimeMillis() - startTime;

            updateTime = timeBuff + millisecondTime;

            seconds = (int) (updateTime / 1000);

            minutes = seconds / 60;

            seconds = seconds % 60;

            milliseconds = (int) (updateTime % 1000);

            stopwatch.setText("" + minutes + ":"
                    + String.format("%02d", seconds));

            handler.postDelayed(this, 0);
        }
    };
}
