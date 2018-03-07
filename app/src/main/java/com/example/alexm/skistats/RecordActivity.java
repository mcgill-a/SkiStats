package com.example.alexm.skistats;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.text.AlphabeticIndex;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

public class RecordActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    String TAG = "SkiStats.Log";

    private ImageButton recordImageButton, pauseImageButton, cancelImageButton, submitImageButton;
    private TextView stopwatch;
    long millisecondTime, startTime, timeBuff, updateTime = 0L;
    int hours, seconds, minutes, milliseconds;
    Handler handler;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        recordImageButton = (ImageButton) findViewById(R.id.recordImageButton);
        pauseImageButton = (ImageButton) findViewById(R.id.pauseImageButton);
        cancelImageButton = (ImageButton) findViewById(R.id.cancelImageButton);
        submitImageButton = (ImageButton) findViewById(R.id.submitImageButton);
        stopwatch = (TextView) findViewById(R.id.stopwatch);

        handler = new Handler();

        // Check permisison
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            // If permission granted
            buildLocationRequest();
            buildLocationCallBack();

            // Create FusedProviderClient
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


            // Set event for button
            recordImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {

                    if (ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RecordActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                        return;
                    }
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    startTime = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable, 0);

                    Toast.makeText(RecordActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
                    // Change state of button
                    recordImageButton.setEnabled(false);
                    pauseImageButton.setEnabled(true);

                }
            });

            pauseImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    timeBuff += millisecondTime;

                    handler.removeCallbacks(runnable);

                    Toast.makeText(RecordActivity.this, "Paused Recording", Toast.LENGTH_SHORT).show();

                    if (ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RecordActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                        return;
                    }
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                    // Change state of button
                    recordImageButton.setEnabled(true);
                    pauseImageButton.setEnabled(false);

                }
            });

            cancelImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Toast.makeText(RecordActivity.this, "Cancelled Recording", Toast.LENGTH_SHORT).show();
                }
            });

            submitImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Toast.makeText(RecordActivity.this, "Submitted Recording", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void buildLocationCallBack()
    {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                for(Location location : locationResult.getLocations())
                {
                    Log.e(TAG,"Lat: " + String.valueOf(location.getLatitude()) + " | Lon: " + String.valueOf(location.getLongitude()) + " | Altitude: " + String.valueOf(location.getAltitude()));
                }
            }
        };
    }

    private void buildLocationRequest()
    {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setSmallestDisplacement(0);
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            millisecondTime = SystemClock.uptimeMillis() - startTime;

            updateTime = timeBuff + millisecondTime;

            hours = (int) ((millisecondTime / (1000*60*60)) % 24);

            seconds = (int) (updateTime / 1000);

            minutes = seconds / 60;

            seconds = seconds % 60;

            milliseconds = (int) (updateTime % 1000);

            if (minutes < 10)
            {
                stopwatch.setText(""  + hours + ":0" + minutes + ":"
                        + String.format("%02d", seconds));
            }
            else
            {
                stopwatch.setText(""  + hours + ":" + minutes + ":"
                        + String.format("%02d", seconds));
            }


            handler.postDelayed(this, 0);
        }
    };
}
