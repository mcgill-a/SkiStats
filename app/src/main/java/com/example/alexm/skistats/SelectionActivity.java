package com.example.alexm.skistats;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
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


    private TextView distanceTotalValue;

    public List<TrackPoint> tPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        distanceTotalValue = (TextView)findViewById(R.id.distanceTotalValue);


        getFileName();
        Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();

        getData();

        calculateTotalDistance();

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

    public void calculateTotalDistance()
    {
        double totalDistance = 0;
        double roundedTotalDistance = 0;
        double distance = 0;
        double maxDistance = 0;
        TrackPoint current;
        TrackPoint next;
        for (int i = 0; i + 1 < tPoints.size(); i++)
        {
            current = tPoints.get(i);
            next = tPoints.get(i+1);
            distance = calculateDistanceBetween(current, next);
            if (distance > maxDistance)
            {
                maxDistance = distance;
            }
            if (distance < 0.043) // Ignore bad inputs (96mph is world record speed (43m/s so 43m, (0.043km/s))
            {
                totalDistance += distance;
            }
        }
        Log.e(TAG, "Max Individual Distance: " + maxDistance);
        Log.e(TAG, filename  + " Total Distance: " + totalDistance);

        roundedTotalDistance = (double)Math.round(totalDistance * 100d) / 100d;
        distanceTotalValue.setText(Double.toString(roundedTotalDistance) + " KM");
        Toast.makeText(getApplicationContext(), " Total Distance: " + roundedTotalDistance + " KM", Toast.LENGTH_LONG).show();
    }

    public double calculateDistanceBetween(TrackPoint current, TrackPoint next)
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

        return (distance / 1000);

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