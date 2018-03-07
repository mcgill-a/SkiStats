package com.example.alexm.skistats;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by AlexM on 07/03/2018.
 */

public class GpsListener implements LocationListener {

    Context context;

    public GpsListener(Context context)
    {
        super();
        this.context = context;
    }

    public Location getLocation()
    {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e("SkiStats.Log.Status","Permissions Error");
            return null;
        }
        try {
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(gpsEnabled)
            {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return location;
            }
            else
            {
                Log.e("SkiStats.Log.Status", "GPS Disabled");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
