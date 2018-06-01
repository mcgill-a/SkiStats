package com.example.alexm.skistats;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

public class MainActivity extends AppCompatActivity {

    private ImageButton recordButton;
    private ImageButton historyButton;
    private ImageButton mapsButton;
    private ImageButton importButton;
    private ImageButton settingsButton;
    private ImageButton exitButton;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2000;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2001;
    private static final int REQUEST_LOCATION = 1000;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                    {
                        Toast.makeText(getApplicationContext(), "Permission was denied.\nIt is required for storing ski location updates", Toast.LENGTH_SHORT).show();
                    }
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
            case REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Permission was denied.\nIt is required for accessing ski data .GPX files", Toast.LENGTH_LONG).show();
                    }
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
            case REQUEST_LOCATION: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Permission was denied.\nDevice location is required for recording ski location updates.", Toast.LENGTH_LONG).show();
                    }
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private boolean ensurePermissionLocation()
    {
        // Check permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return false;
        }
        else
        {
            // Permission granted
            return true;
        }
    }

    private boolean ensurePermissionWrite()
    {
        // Check permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        else
        {
            // Permission granted
            return true;
        }
    }

    private boolean ensurePermissionRead()
    {
        // Check permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            return false;
        }
        else
        {
            // Permission granted
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main_tile);
        initButtons();

        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(ensurePermissionLocation() && ensurePermissionWrite())
                {
                    startActivity(new Intent(MainActivity.this, RecordActivity.class));
                }
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(ensurePermissionRead())
                {
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                }
            }
        });

        mapsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(ensurePermissionLocation())
                {
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                }
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(ensurePermissionRead())
                {
                    startActivity(new Intent(MainActivity.this, ImportActivity.class));
                }
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