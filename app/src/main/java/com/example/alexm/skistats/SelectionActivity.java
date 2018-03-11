package com.example.alexm.skistats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class SelectionActivity extends FragmentActivity implements OnMapReadyCallback, Serializable{

    private GoogleMap mMap;
    private String absoluteFilepath;
    private String TAG = "SkiStats.Log";
    private GPXParser mParser = new GPXParser();

    private SkiVector skiVector = new SkiVector();
    private ArrayList<String> altitudes = new ArrayList<>();

    private TextView distanceTotalValue;
    private TextView distanceSkiValue;
    private TextView distanceSkiLiftValue;
    private TextView altitudeMaxValue;
    private TextView altitudeMinValue;
    private TextView skiTimeValue;
    private TextView skiLiftTimeValue;
    private TextView skiTotalTimeValue;
    private TextView gpsStartTimeValue;
    private TextView gpsEndTimeValue;
    private TextView speedAverageValue;

    private Button buttonShare;
    private Button buttonExport;
    private Button buttonAltitude;

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
    public List<TrackPoint> tPoints = new ArrayList<>();
    //public List<TrackPoint> tPointsFiltered = new ArrayList<>(); // this would be the kalman filtered gps data

    public void initialise()
    {
        distanceTotalValue = (TextView)findViewById(R.id.distanceTotalValue);
        distanceSkiValue = (TextView)findViewById(R.id.distanceSkiValue);
        distanceSkiLiftValue = (TextView)findViewById(R.id.distanceSkiLiftValue);
        altitudeMaxValue = (TextView)findViewById(R.id.altitudeMaxValue);
        altitudeMinValue = (TextView)findViewById(R.id.altitudeMinValue);
        skiTimeValue = (TextView)findViewById(R.id.skiTimeValue);
        skiLiftTimeValue = (TextView)findViewById(R.id.skiLiftTimeValue);
        skiTotalTimeValue = (TextView)findViewById(R.id.totalTimeValue);
        gpsStartTimeValue = (TextView)findViewById(R.id.gpsStartTimeValue);
        gpsEndTimeValue = (TextView)findViewById(R.id.gpsEndTimeValue);
        speedAverageValue = (TextView)findViewById(R.id.speedAverageValue);

        buttonShare = (Button)findViewById(R.id.btnShare);
        buttonExport = (Button)findViewById(R.id.btnExport);
        buttonAltitude = (Button)findViewById(R.id.btnAltitude);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        initialise();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getFileName();
        if(getData() != -1)
        {
            statCalculations();
            setTextVales();
        }
        else
        {
            gpxReadFailed();
        }

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                shareSocial();
            }
        });

        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                exportFile();
            }
        });

        buttonAltitude.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(SelectionActivity.this, PopupChart.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("alts", altitudes);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }

    public void exportFile()
    {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/xml");
        File f = new File(absoluteFilepath);
        String path = f.getAbsolutePath();
        if(f.exists())
        {
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
            startActivity(Intent.createChooser(share, "Export Your Recording"));
        }
    }


    public Bitmap shareScreen(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),(view.getHeight()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void shareSocial()
    {
        String folder = Environment.getExternalStorageDirectory() + File.separator + "SkiStats/Share/";
        clearSharedImagesFolder(folder);
        Bitmap icon = shareScreen((this.getWindow().getDecorView().findViewById(android.R.id.content)));
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String jpgExtension = ".jpg";
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "SkiStats/Share/Stats" + jpgExtension);
        int index = 1;
        while(f.exists())
        {
            index++;
            f = new File(Environment.getExternalStorageDirectory() + File.separator + "SkiStats/Share/Stats" + index + jpgExtension);
        }
        try {
            f.getParentFile().mkdirs();
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Error: " + e.getMessage());
        }
        String path = f.getAbsolutePath();
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
        startActivity(Intent.createChooser(share, "Share Your Stats"));
    }

    public void clearSharedImagesFolder(String directory)
    {
        File dir = new File(directory);
        // loop through each file in directory

        File[] allFiles = dir.listFiles();
        if (allFiles != null)
        {
            for (File image : allFiles)
            {
                image.delete();
                if(image.exists())
                {
                    getApplicationContext().deleteFile(image.getName());
                }
                // To ensure the file is removed on Windows Operating System also
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{image.getAbsolutePath()}, null, null);
            }
        }
    }

    public void getFileName()
    {
        absoluteFilepath = (String) getIntent().getStringExtra("filename");
    }

    public void getName(String name)
    {
        // get display name for share image file
    }

    public int getData() {
        Gpx parsedGpx = null;

        LatLng latlng;
        try {
            if (absoluteFilepath.charAt(0) == '/')
            {
                StringBuilder sb = new StringBuilder(absoluteFilepath);
                sb.deleteCharAt(0);
                absoluteFilepath = sb.toString();
            }
            Log.e(TAG,"FilePath: " + absoluteFilepath);
            File file = new File(absoluteFilepath);
            InputStream in = new FileInputStream(file);
            parsedGpx = mParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        if (parsedGpx != null) {
            //Integer count = 0;
            List<Track> tracks = parsedGpx.getTracks();
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                //Log.e(TAG, "track " + i + ":");
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(i);
                    //Log.e(TAG, " segment " + j + ":");
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
                        tPoints.add(trackPoint);
                    }
                }
            }
            return 1;
        }
        return -1;
    }

    public void gpxReadFailed()
    {
        Log.e(TAG, "Error parsing gpx file");
        Toast.makeText(getApplicationContext(), "GPX File Read Failed", Toast.LENGTH_SHORT).show();
    }

    public void statCalculations()
    {
        double totalDistance = 0;
        double totalSkiLiftDistance = 0;
        double totalSkiDistance = 0;

        int totalSkiTime = 0;
        int totalSkiLiftTime = 0;
        int totalTime = 0;


        DateTime gpsStartTime;
        DateTime gpsEndTime;

        double distance = 0;
        double height = 0;
        double maxDistance = 0;
        int count = 0;

        SkiVector vector;

        TrackPoint current;
        TrackPoint next;

        double averageSpeed = 0;
        double speed = 0;
        double maxSpeed = 0;
        double maxspeedAvgHeight = 0;
        int maxSpeedTime = 0;
        int time = 0;
        int maxTime = 0;
        int timeCount = 0;
        int timeCount2 = 0;
        int marker = 0;
        double altitude = 0;
        double maxAltitude = 0;
        double minAltitude = Double.MAX_VALUE;

        double avgheight = 0;
        double a = 0;
        double b = 0;
        double c = 0;


        double gradient = 0;
        int gradientCount = 0;
        double averageGradient = 0;
        double maxGradient = 0;

        int pauseCount = 0;

        gpsStartTime = tPoints.get(0).getTime().toDateTime();
        gpsEndTime = tPoints.get(tPoints.size()-1).getTime().toDateTime();

        for (int i = 0; i + 1 < tPoints.size(); i++)
        {
            count++;
            current = tPoints.get(i);
            next = tPoints.get(i+1);

            Seconds seconds = Seconds.secondsBetween(current.getTime(), next.getTime());

            time = seconds.getSeconds();

            // If the time between the current track point and the next track point is more than x minutes,
            // skip to the next one as the gps recording isn't useful
            if (time > 60)
            {
                pauseCount++;
                continue;
            }

            // SkiVector object - contains height and distance
            vector = calculateDistanceBetween(current, next);
            distance = vector.getDistance();
            height = vector.getHeight();
            altitude = current.getElevation();


            if (altitude > maxAltitude)
            {
                maxAltitude = altitude;
            }

            if (altitude < minAltitude)
            {
                minAltitude = altitude;
            }

            speed = distance / time;
            // Try to filter out invalid results (for example - when gps was paused and then resumed at another location
            if ((time > 0 && time < 40) && (speed < 0.0277)) // speed < 100km/h 0.0277
            {
                altitudes.add(current.getElevation().toString());
                // TOTAL DISTANCE
                if (distance > maxDistance)
                {
                    maxDistance = distance;
                }
                totalDistance += distance;

                // check if on ski lift or skiing
                // check multiple distances to ensure that they are likely on a lift
                // check average of 3 heights, 1 before 1 now 1 after. If average is uphill then lift

                    if (i > 1 && (i < tPoints.size() - 2)) // so doesnt go out of bounds >> (0-1) or (size+2)
                    {
                        a = ((tPoints.get(i - 1).getElevation()) - (tPoints.get(i).getElevation()));
                        b = ((tPoints.get(i).getElevation()) - (tPoints.get(i + 1).getElevation()));
                        c = ((tPoints.get(i + 1).getElevation()) - (tPoints.get(i + 2).getElevation()));
                        avgheight = (a + b + c) / 3;
                    }
                    else
                    {
                        avgheight = 1; // set avg to 1 (positive height increase, so not on ski lift) - just a flag, value doesn't matter.
                    }

                if(avgheight < 0)
                {
                    // user is on a ski lift

                    totalSkiLiftDistance += distance;
                    totalSkiLiftTime += time;
                }
                else
                {
                    // user is skiing
                    totalSkiDistance += distance;
                    totalSkiTime += time;

                    if (speed > maxSpeed)
                    {
                        maxSpeed = speed;
                        // only getting avgheight and time to help debugging high max speed errors
                        maxspeedAvgHeight = avgheight;
                        maxSpeedTime = time;
                    }
                    averageSpeed += speed;
                    gradientCount++;

                    // REMOVED DUE TO BEING TOO INACCURATE
                    // Gradient (rise/run) x 100
                    /*double rise = height;
                    double run = distance * 1000;
                    gradient = (rise / run) * 100;

                    if (gradient > maxGradient)
                    {
                        maxGradient = gradient;
                    }

                    averageGradient += gradient; */
                }

                // only get speed info when gps update time is less than 10s. reduces error
                /*if (time < 10)
                {
                    if (speed > maxSpeed && height < 0)
                    {
                        maxSpeed = speed;
                        marker = i;
                    }
                    averageSpeed += speed;
                } */
            }
        }
        averageSpeed = averageSpeed / gradientCount;
        averageGradient = averageGradient / gradientCount;
        totalTime = totalSkiTime + totalSkiLiftTime;
        String totalSkiTimeString = splitToComponentTimes(totalSkiTime);
        String totalSkiLiftTimeString = splitToComponentTimes(totalSkiLiftTime);
        String totalTimeString = splitToComponentTimes(totalTime);

        maxSpeed = maxSpeed * 3600; // convert from km/s to km/h
        averageSpeed = averageSpeed * 3600; // convert from km/s to km/h

        //TrackPoint marked = tPoints.get(643);
        //TrackPoint markedNext = tPoints.get(644);
        //SkiVector dist = calculateDistanceBetween(marked, markedNext);
        //Log.e(TAG, "Marker: " + marker);
        //Log.e(TAG, "Marked: " + dist.toString() + " | TIME 1: " + marked.getTime() + " TIME 2: " + markedNext.getTime());
        //Log.e(TAG, "Max Time:  " + maxTime);
        //Log.e(TAG, "Time Count:  " + timeCount);
        //Log.e(TAG, "Time Count > 60:  " + timeCount2);
        Log.e(TAG, "Max Individual Distance: " + maxDistance);
        Log.e(TAG,"Total Distance: " + totalDistance);
        Log.e(TAG,"Total Ski Distance: " + totalSkiDistance);
        Log.e(TAG,"Total Ski Lift Distance: " + totalSkiLiftDistance);

        Log.e(TAG,"Total Ski Time: " + totalSkiTimeString);
        Log.e(TAG,"Total Ski Lift Time: " + totalSkiLiftTimeString);
        Log.e(TAG,"Total Time: " + totalTimeString) ;

        Log.e(TAG,"Max Altitude: " + maxAltitude);
        Log.e(TAG,"Min Altitude: " + minAltitude);

        Log.e(TAG,"Pause Count: " + pauseCount);
        Log.e(TAG,"Max Gradient: "  + maxGradient);
        Log.e(TAG,"Average Gradient: " + averageGradient);

        Log.e(TAG, "Max Speed: " + maxSpeed);
        Log.e(TAG, "Max Speed avgheight: " + maxspeedAvgHeight);
        Log.e(TAG, "Max Speed time: " + maxSpeedTime);
        Log.e(TAG, absoluteFilepath + " Average Speed: " + averageSpeed);

        double roundedTotalDistance = (double)Math.round(totalDistance * 100d) / 100d;
        double roundedSkiDistance = (double)Math.round(totalSkiDistance * 100d) / 100d;
        double roundedSkiLiftDistance = (double)Math.round(totalSkiLiftDistance * 100d) / 100d;

        double roundedAverageSpeed = (double)Math.round(averageSpeed * 10d) / 10d;

        int altMaxInteger = (int) maxAltitude;
        int altMinInteger = (int) minAltitude;

        distanceTotalValue.setText(Double.toString(roundedTotalDistance) + " KM");
        distanceSkiValue.setText(Double.toString(roundedSkiDistance) + " KM");
        distanceSkiLiftValue.setText(Double.toString(roundedSkiLiftDistance) + " KM");
        altitudeMaxValue.setText(altMaxInteger + "m");
        altitudeMinValue.setText(altMinInteger + "m");
        skiTimeValue.setText(totalSkiTimeString);
        skiLiftTimeValue.setText(totalSkiLiftTimeString);
        skiTotalTimeValue.setText(totalTimeString);
        gpsStartTimeValue.setText("STARTED: " + fmt.print(gpsStartTime));
        gpsEndTimeValue.setText("FINISHED: " + fmt.print(gpsEndTime));
        speedAverageValue.setText(Double.toString(roundedAverageSpeed) + " KM/H");
    }

    public static String splitToComponentTimes(Integer secondsTotal)
    {
        long longVal = new Long(secondsTotal);
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        String output = hours + "h : " + mins + "m : " + secs + "s";
        return output;
    }

    public SkiVector calculateDistanceBetween(TrackPoint current, TrackPoint next)
    {
        double distance = 0;
        final int earthRadius = 6371;
        double lat1 = current.getLatitude();
        double lon1 = current.getLongitude();
        double ele1 = current.getElevation();
        double lat2 = next.getLatitude();
        double lon2 = next.getLongitude();
        double ele2 = next.getElevation();

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        distance = earthRadius * c * 1000;
        double height = ele1 - ele2;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);
        distance = Math.sqrt(distance);

        skiVector.setDistance(distance / 1000);
        skiVector.setHeight(height);
        return (skiVector);
    }

    public void processMap(GoogleMap googleMap)
    {
        if (googleMap!= null && tPoints.size() != 0)
        {
            PolylineOptions poption = new PolylineOptions();
            LatLng latlng;
            for(int i = 0; i < tPoints.size(); i++)
            {
                latlng = new LatLng(tPoints.get(i).getLatitude(), tPoints.get(i).getLongitude());
                poption.add(latlng);
            }
            poption.width(9).color(Color.BLUE).geodesic(true);

            googleMap.addPolyline(poption);
            LatLng firstLatLng = new LatLng(tPoints.get(0).getLatitude(), tPoints.get(0).getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 14));
        }
    }

    public void setTextVales()
    {
        // Nothing rn but will eventually pass in all values from calculcate stats and set the text views from here
        //distanceTotalValue.setText();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        processMap(googleMap);
        // Add a marker in Sydney and move the camera
       // LatLng sauze = new LatLng(45.0269, 6.8584);
       // mMap.addMarker(new MarkerOptions().position(sauze).title("Marker in Sauze d'Oulx"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sauze));
    }
}