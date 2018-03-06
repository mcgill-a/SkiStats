package com.example.alexm.skistats;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class SelectionActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String filename;
    //private List<LatLng> latlongs = new ArrayList<>();
    private String TAG = "SkiStats.Log";
    private GPXParser mParser = new GPXParser();

    private SkiVector skiVector = new SkiVector();

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

    private Button buttonAltitude;



    private DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
    public List<TrackPoint> tPoints = new ArrayList<>();
    //public List<TrackPoint> tPointsFiltered = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        buttonAltitude = (Button)findViewById(R.id.btnAltitude);

        buttonAltitude.setOnClickListener(new View.OnClickListener()  {
           @Override
            public void onClick(View v)
           {
               startActivity(new Intent(SelectionActivity.this, PopupChart.class));
           }
        });

        getFileName();
        Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();

        getData();

        statCalculations();

        setTextVales();

    }

    public void getData() {
        Gpx parsedGpx = null;

        LatLng latlng;
        try {
            InputStream in = getAssets().open("gpsData/sauze/"+ filename);
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
            //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Error parsing gpx file");
            Toast.makeText(getApplicationContext(), "GPX File Read Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void statCalculations()
    {
        double totalDistance = 0;
        double totalSkiLiftDistance = 0;
        double totalSkiDistance = 0;

        int totalSkiTime = 0;
        int totalSkiLiftTime = 0;
        int totalTime = 0;


        LocalTime gpsStartTime;
        LocalTime gpsEndTime;

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

        gpsStartTime = tPoints.get(0).getTime().toLocalTime();
        gpsEndTime = tPoints.get(tPoints.size()-1).getTime().toLocalTime();

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

            if (distance < 0.05) // Ignore bad inputs (96mph is world record speed (43m/s so 43m, (0.043km/s))
            {
                //totalDistance += distance;
            }
            speed = distance / time;
            // filter out invalid results (for example - when gps was paused and then resumed at another location
            if ((time > 0 && time < 60) && speed < 0.0277) // speed < 100km/h 0.0277
            {
                // TOTAL DISTANCE
                if (distance > maxDistance)
                {
                    maxDistance = distance;
                }
                totalDistance += distance;

                // check if on ski lift or skiing
                // should really check multiple distances to ensure that they are definitely on a lift
                // how?
                // check average of 3 heights, 1 before 1 now 1 after. If average is uphill then lift


                    if (i > 1 && (i < tPoints.size() - 2)) // so doesnt go out of bounds (0-1) (size+2)
                    {
                        a = ((tPoints.get(i - 1).getElevation()) - (tPoints.get(i).getElevation()));
                        b = ((tPoints.get(i).getElevation()) - (tPoints.get(i + 1).getElevation()));
                        c = ((tPoints.get(i + 1).getElevation()) - (tPoints.get(i + 2).getElevation()));
                        avgheight = (a + b + c) / 3;
                    }
                    else
                    {
                        avgheight = 1; // set avg to 1 (poisitive height increase, so not on ski lift)
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
                    }
                    averageSpeed += speed;
                    gradientCount++;
                    // Gradient (rise/run) x 100
                    double rise = height;
                    double run = distance * 1000;
                    gradient = (rise / run) * 100;

                    if (gradient > maxGradient)
                    {
                        maxGradient = gradient;
                    }

                    averageGradient += gradient;
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

            //DateTime dt = new DateTime(current.getTime());
            //DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            //Log.e(TAG, seconds.getSeconds());
            // AVERAGE SPEED
            //timeDifference = (current.getTime() - next.getTime());
            //DateTime dtNext = new DateTime(next.getTime());
            if (time > maxTime)
            {
                maxTime = time;
            }

            if (time > 15 && time < 60) // ignore the speed
            {
                //timeCount++;
            }
            if (time > 60) // ignore the speed and distance
            {
                timeCount2++;
            }

            if (time == 1)
            {
                //marker++;
            }
            timeCount++;

            /*if (time > 0 && time < 60) //&& distance < 0.043)
            {
                speed = distance / time;
                if (speed > maxSpeed)
                {
                    maxSpeed = speed;
                    //marker = i;
                }
                //averageSpeed += speed;
            }*/
            if (time != 0 && speed < 0.02)
            {
                /*if (speed > maxSpeed)
                {
                    maxSpeed = speed;
                }
                averageSpeed += speed; */
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


        TrackPoint marked = tPoints.get(643);
        TrackPoint markedNext = tPoints.get(644);
        SkiVector dist = calculateDistanceBetween(marked, markedNext);
        //Log.e(TAG, "Marker: " + marker);
        //Log.e(TAG, "Marked: " + dist.toString() + " | TIME 1: " + marked.getTime() + " TIME 2: " + markedNext.getTime());
        //Log.e(TAG, "Max Time:  " + maxTime);
        //Log.e(TAG, "Time Count:  " + timeCount);
        //Log.e(TAG, "Time Count > 60:  " + timeCount2);
        //Log.e(TAG, "Max Individual Distance: " + maxDistance);
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
        Log.e(TAG, filename + " Average Speed: " + averageSpeed);

        double roundedTotalDistance = (double)Math.round(totalDistance * 100d) / 100d;
        double roundedSkiDistance = (double)Math.round(totalSkiDistance * 100d) / 100d;
        double roundedSkiLiftDistance = (double)Math.round(totalSkiLiftDistance * 100d) / 100d;

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


        //Toast.makeText(getApplicationContext(), " Total Distance: " + roundedTotalDistance + " KM", Toast.LENGTH_LONG).show();
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

        /*if (height < 0)
        {
            // for only recording ski distance downhill return -1;
        }
        else
        {
            //return (distance / 1000);
        } */

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

    public void getFileName()
    {
        filename = (String) getIntent().getStringExtra("filename");
    }
}