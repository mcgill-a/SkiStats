package com.example.alexm.skistats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import net.danlew.android.joda.JodaTimeAndroid;

public class MainActivity extends AppCompatActivity {

    private ImageButton recordButton;
    private ImageButton historyButton;
    private ImageButton mapsButton;
    private ImageButton importButton;
    private ImageButton settingsButton;
    private ImageButton exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main_tile);
        initButtons();

        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RecordActivity.class));
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });

        mapsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ImportActivity.class));
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void initButtons() {
        recordButton = (ImageButton) findViewById(R.id.img_btn_record);
        historyButton = (ImageButton) findViewById(R.id.img_btn_history);
        mapsButton = (ImageButton) findViewById(R.id.img_btn_maps);
        importButton = (ImageButton) findViewById(R.id.img_btn_import);
        settingsButton = (ImageButton) findViewById(R.id.img_btn_settings);
        exitButton = (ImageButton) findViewById(R.id.img_btn_exit);
    }
}