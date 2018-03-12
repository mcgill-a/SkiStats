package com.example.alexm.skistats;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.AlphabeticIndex;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private static GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_CODE = 1000;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    String TAG = "SkiStats.Log";
    private List<Location> locations = new ArrayList<Location>();


    private ImageButton recordImageButton, pauseImageButton, cancelImageButton, submitImageButton;
    private TextView stopwatch;
    long millisecondTime, startTime, timeBuff, updateTime = 0L;
    int hours, seconds, minutes, milliseconds;
    Handler handler;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2000;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //locationTracker.stopLocationService(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                    {
                        Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show();
                    }
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    public boolean checkPermissions()
    {
        if (ContextCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            if (ActivityCompat.shouldShowRequestPermissionRationale(RecordActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {


                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this,"External Storage permission is needed to store GPS (.gpx) files",Toast.LENGTH_SHORT).show();
                return false;
            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(RecordActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }
        else
        {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        if (!checkPermissions())
        {
            checkPermissions();
            Toast.makeText(this,"External Storage permission is needed to store GPS (.gpx) files",Toast.LENGTH_LONG).show();
        }

        if(!checkPermissions())
        {
            finish();
        }


        recordImageButton = (ImageButton) findViewById(R.id.recordImageButton);
        pauseImageButton = (ImageButton) findViewById(R.id.pauseImageButton);
        //cancelImageButton = (ImageButton) findViewById(R.id.cancelImageButton);
        submitImageButton = (ImageButton) findViewById(R.id.submitImageButton);
        stopwatch = (TextView) findViewById(R.id.stopwatch);

        handler = new Handler();

        // Check permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {

            /*
            //Without Google API Client Auto Location Dialog will not work
            mGoogleApiClient = new GoogleApiClient.Builder(RecordActivity.this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            startService(new Intent(this, MyLocationService.class));
            */
            // If permission granted
            buildLocationRequest();
            buildLocationCallBack();

            // Create FusedProviderClient
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            // Set event for button
            recordImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                /*locationTracker = new LocationTracker("my.action")
                        .setInterval(1000)
                        .setGps(true)
                        .setNetWork(false)
                        .start(getBaseContext(),RecordActivity.this); */



                    if (ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RecordActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                        return;
                    }
                    Log.d(TAG, "GPS Recording Started");
                    // Original gps location provider
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
                    Log.d(TAG, "GPS Recording Paused");
                    timeBuff += millisecondTime;
                    //locationTracker.stopLocationService(RecordActivity.this);
                    handler.removeCallbacks(runnable);

                    Toast.makeText(RecordActivity.this, "Paused Recording", Toast.LENGTH_SHORT).show();

                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                    // Change state of button
                    recordImageButton.setEnabled(true);
                    pauseImageButton.setEnabled(false);

                }
            });

            submitImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if(pauseImageButton.isEnabled())
                    {
                        pauseImageButton.performClick();
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(RecordActivity.this);
                    alert.setTitle("Save Recording");
                    alert.setMessage("Do you want to save this recording?");

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch(i)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                    // User selects Yes
                                    // do a thing to cancel recording
                                    Log.d(TAG, "GPS Recording - Saving in progress..");
                                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                    // Prompt user for filename
                                    String filename = "Ski Activity";
                                    String extension = ".gpx";
                                    //String filename = "SS_" + df.format(new Date(locations.get(0).getTime()));

                                    String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
                                    // add loop to increment file name if it exists
                                    File dir = new File(path);
                                    if (!dir.exists())
                                    {
                                        dir.mkdirs();
                                    }
                                    File file = new File(path + filename + extension);
                                    String newFileName = "";
                                    int index = 1;
                                    while(file.exists())
                                    {
                                        index++;
                                        newFileName = filename + " " + index;
                                        file = new File(path + newFileName + extension);
                                    }
                                    String fullFileName;
                                    if (newFileName.length() > 2)
                                    {
                                        fullFileName = newFileName + extension;
                                        filename = newFileName;
                                    }
                                    else
                                    {
                                        fullFileName = filename + extension;
                                    }

                                    String fullnamePath = path + fullFileName;

                                    File recording = new File(dir, fullFileName);
                                    // CHANGE TO HIGHER NUMBER (ONLY LOW FOR TESTING)
                                    Log.d(TAG,"Trying to save");
                                    if(locations.size() > 3)
                                    {
                                        writePath(recording, filename, locations);
                                        Toast.makeText(RecordActivity.this, "Recording Saved", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG,"GPS Recording - Saved: " + filename);
                                        resetStopwatch();
                                    }
                                    else
                                    {
                                        Log.d(TAG,"Error: Not enough GPS data recorded");
                                        Toast.makeText(getApplicationContext(), "Error: Not enough GPS data recorded",Toast.LENGTH_LONG).show();
                                    }


                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    // User clicked No
                                    Log.d(TAG,"GPS Recording - Discarded");
                                    resetStopwatch();
                                    locations.clear();
                                    break;
                            }
                        }
                    };

                    alert.setPositiveButton("Save", dialogClickListener);
                    alert.setNeutralButton("Back", dialogClickListener);
                    alert.setNegativeButton("Discard", dialogClickListener);

                    AlertDialog dialog = alert.create();
                    // Display the alert
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(!recordImageButton.isEnabled())
        {
            // ask user to submit or discard
            submitImageButton.performClick();
        }
        else
        {
            // exit normally
            finish();
        }


        //moveTaskToBack(true);
    }

    public void resetStopwatch()
    {
        millisecondTime = 0L;
        startTime = 0L;
        timeBuff = 0L;
        updateTime = 0L;
        locations.clear();
        stopwatch.setText("0:00:00");
    }

    public static void writePath(File file, String n, List<Location> points)
    {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"SkiStats\">\n<trk>\n";
        String name = "<name>" + n + "</name>\n<trkseg>\n";

        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        for (Location l : points) {
            segments += "<trkpt lat=\"" + l.getLatitude() + "\" lon=\"" + l.getLongitude() + "\">\n<ele>" + l.getAltitude() +"</ele>\n<time>" + df.format(new Date(l.getTime())) + "</time></trkpt>\n";
        }

        String footer = "</trkseg>\n</trk>\n</gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
            Log.d("SkiStats.Log.Status", "Saved " + points.size() + " points.");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d("SkiStats.Log.Status", "Error Writting Path",e);
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
                    Log.d(TAG,"Lat: " + String.valueOf(location.getLatitude()) + " | Lon: " + String.valueOf(location.getLongitude()) + " | Altitude: " + String.valueOf(location.getAltitude()));
                    if(location.getAccuracy() > 0)
                    {
                        locations.add(location);
                    }
                }
            }
        };
    }

    private void buildLocationRequest()
    {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
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
