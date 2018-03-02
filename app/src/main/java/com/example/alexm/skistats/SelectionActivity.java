package com.example.alexm.skistats;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private List<LatLng> latlongs = new ArrayList<>();
    private String TAG = "SkiStats.Log";
    public GPXParser mParser = new GPXParser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getFileName();
        Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();

        getData();


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
                Log.e(TAG, "track " + i + ":");
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(i);
                    Log.e(TAG, " segment " + j + ":");
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
                        latlng = new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude());
                        latlongs.add(latlng);
                        //Log.e(TAG, "   point: lat" + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude() + ", time " + trackPoint.getTime());
                        //count++;
                    }
                }
            }
            //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Error parsing gpx file");
            Toast.makeText(getApplicationContext(), "GPX File Read Failed", Toast.LENGTH_SHORT).show();
        }
    }


    public void processMap(GoogleMap googleMap)
    {
        if (googleMap!= null && latlongs.size() != 0)
        {
            PolylineOptions poption = new PolylineOptions();

            for(int i = 0; i < latlongs.size(); i++)
            {
                poption.add(latlongs.get(i));
            }
            poption.width(8).color(Color.BLUE).geodesic(true);

            googleMap.addPolyline(poption);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlongs.get(0), 14));

        }
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
