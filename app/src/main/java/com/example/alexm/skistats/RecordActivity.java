package com.example.alexm.skistats;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.support.v7.widget.DialogTitle;
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

import org.joda.time.DateTime;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

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

        while (!checkPermissions())
        {
            checkPermissions();
            Toast.makeText(this,"External Storage permission is needed to store GPS (.gpx) files",Toast.LENGTH_LONG).show();
        }

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
                    Log.e(TAG, "GPS Recording Started");
                    if (ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RecordActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                        return;
                    }

                    /* GpsListener listener = new GpsListener(getApplicationContext());
                    Location location = listener.getLocation();
                    if (location == null)
                    {
                        Toast.makeText(getApplicationContext(),"GPS unable to return value",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Log.e(TAG,"Lat: " + String.valueOf(location.getLatitude()) + " | Lon: " + String.valueOf(location.getLongitude()) + " | Altitude: " + String.valueOf(location.getAltitude()));
                        locations.add(location);
                    } */

                    // Orgiginal gps location provider
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
                    Log.e(TAG, "GPS Recording Paused");
                    timeBuff += millisecondTime;

                    handler.removeCallbacks(runnable);

                    Toast.makeText(RecordActivity.this, "Paused Recording", Toast.LENGTH_SHORT).show();

                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                    // Change state of button
                    recordImageButton.setEnabled(true);
                    pauseImageButton.setEnabled(false);

                }
            });

            cancelImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    //Toast.makeText(RecordActivity.this, "Cancelled Recording", Toast.LENGTH_SHORT).show();
                    pauseImageButton.performClick();
                    AlertDialog.Builder alert = new AlertDialog.Builder(RecordActivity.this);
                    alert.setTitle("Discard Recording");
                    alert.setMessage("Are you sure you want to discard the recording?");

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch(i)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                    // User selects Yes
                                    // do a thing to cancel recording
                                    Log.e(TAG, "GPS Recording Discarded");
                                    resetStopwatch();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    // User clicked No
                                    break;
                            }
                        }
                    };

                    alert.setPositiveButton("Yes", dialogClickListener);
                    alert.setNegativeButton("No", dialogClickListener);

                    AlertDialog dialog = alert.create();
                    // Display the alert
                    dialog.show();
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
                                    Log.e(TAG, "GPS Recording - Saving in progress..");
                                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                    String filename = "SS_" + df.format(new Date(locations.get(0).getTime()));

                                    String path = Environment.getExternalStorageDirectory() + "/" +  "SkiStats/GPS/Recordings/";
                                    File dir = new File(path);
                                    if (!dir.exists())
                                    {
                                        dir.mkdirs();
                                    }
                                    String fullFileName = filename + ".gpx";
                                    String fullnamePath = path + fullFileName;

                                    File recording = new File(dir, fullFileName);
                                    // CHANGE TO HIGHER NUMBER (ONLY LOW FOR TESTING)
                                    if(locations.size() > 2)
                                    {
                                        writePath(recording, filename, locations);
                                        Toast.makeText(RecordActivity.this, "Recording Saved", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG,"GPS Recording - Saved");
                                        resetStopwatch();
                                    }
                                    else
                                    {
                                        Log.e(TAG,"Error: Not enough GPS data recorded");
                                    }


                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    // User clicked No
                                    Log.e(TAG,"GPS Recording - Discarded");
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
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>" + "\n" +
                "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"SkiStats\" version=\"1.1\"" + "\n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 " +
                "http://www.topografix.com/GPX/1/1/gpx.xsd\">" + "\n" + "<trk>\n";

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        DateTime currentTime = new DateTime();
        //String metadata = "<metadata>" + "\n" + "<time>" + df.format(new DateTime(currentTime));

        String name = "<name>" + n + "</name>" + "\n" + "<trkseg>\n";

        String segments = "";

        for (Location l : points) {
            segments += "<trkpt lat=\"" + l.getLatitude() + "\" lon=\"" + l.getLongitude() + "\">" + "\n" +
                    "<ele=" + l.getAltitude() + "</ele>\n" +  "<time>" + df.format(new Date(l.getTime())) + "</time></trkpt>\n";
        }

        String footer = "</trkseg>" + "\n" + "</trk>" + "\n" + "</gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            //writer.append(metadata);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
            Log.e("SkiStats.Log.Status", "Saved " + (points.size() -1) + " points.");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("SkiStats.Log.Status", "Error Writting Path",e);
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
