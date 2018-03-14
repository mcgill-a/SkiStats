package com.example.alexm.skistats;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;
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
        setTheme(R.style.AppTheme_NoActionBar);
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
        submitImageButton = (ImageButton) findViewById(R.id.submitImageButton);
        stopwatch = (TextView) findViewById(R.id.stopwatch);

        handler = new Handler();

        // Check permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {

            // If permission granted

            LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, new IntentFilter("backgroundGpsUpdates"));

            final Intent intent = new Intent(RecordActivity.this, ServiceLocation.class);
            // Set event for button
            recordImageButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {

                    if (ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RecordActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                        return;
                    }

                    if (Build.VERSION.SDK_INT < 26) {
                        startService(intent);
                    } else {
                        startForegroundService(intent);
                    }

                    Log.d(TAG, "GPS Recording Started");

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

                    // stop getting gps updates
                    stopService(intent);


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
                                    String filename = "Ski Activity 1";
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
                                    if(file.exists())
                                    {
                                        StringBuilder sb = new StringBuilder(filename);
                                        sb.deleteCharAt(sb.length()-1);
                                        sb.deleteCharAt(sb.length()-1);
                                        filename = sb.toString();
                                    }
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
                                    Log.d(TAG,"Attempting to save recording");
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
                                        locations.clear();
                                        resetStopwatch();
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

    private BroadcastReceiver messageReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Double latitude = intent.getDoubleExtra("latitude", 0);
            Double longitude = intent.getDoubleExtra("longitude", 0);
            Double altitude = intent.getDoubleExtra("altitude", 0);
            Long time = intent.getLongExtra("time",0);

            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAltitude(altitude);
            location.setTime(time);
            if  (locations.size() > 1)
            {
                if (location.getTime() != locations.get(locations.size()-1).getTime())
                {
                    locations.add(location);
                }
            }
            else
            {
                locations.add(location);
            }
        }
    };

    @Override
    public void onBackPressed() {
        if(!recordImageButton.isEnabled())
        {
            // ask user to submit or discard recording
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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
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
            Log.e("SkiStats.Log.Status", "Error Writting Path",e);
        }
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
