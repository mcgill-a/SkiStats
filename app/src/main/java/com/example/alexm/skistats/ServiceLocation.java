package com.example.alexm.skistats;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by AlexM on 12/03/2018.
 */

public class ServiceLocation extends Service {
    private static final String TAG = "SkiStats.Log.GPS";
    private LocationManager mLocationManager = null;
    private static final int NOTIFICATION_ID = 2000;

    private static final float LOCATION_UPDATE_DISTANCE = 0f;
    private static int LOCATION_UPDATE_INTERVAL = 1000;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;


        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);

            mLastLocation.set(location);

            Intent intent = new Intent("backgroundGpsUpdates");
            sendLocationUpdate(intent);

        }

        private void sendLocationUpdate(Intent intent)
        {
            intent.putExtra("latitude", mLastLocation.getLatitude());
            intent.putExtra("longitude", mLastLocation.getLongitude());
            intent.putExtra("altitude",mLastLocation.getAltitude());
            intent.putExtra("time",mLastLocation.getTime());
            LocalBroadcastManager.getInstance(ServiceLocation.this).sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand called");
        super.onStartCommand(intent, flags, startId);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.RED);
            //notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.d_ski)
                .setVibrate(new long[] {0L})
                .setColor(2)
                .setContentTitle("Ski Stats Recording")
                .setContentText("Currently Tracking GPS Location")
                .setContentInfo("Info");

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate called");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "Failed to submit location request", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "Failed to find network provider " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy called");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "Failed to remove the service location listener", ex);
                }
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Log.e(TAG,"onTaskRemoved");

        stopSelf();
    }

    private void initializeLocationManager() {

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean batterySaverEnabled = SP.getBoolean("battery_saver",false);
        if(batterySaverEnabled)
        {
            LOCATION_UPDATE_INTERVAL = 4000;
        }
        else
        {
            LOCATION_UPDATE_INTERVAL = 1000;
        }

        Log.e(TAG, "Location Manager Preset - Minimum Interval: "+ LOCATION_UPDATE_INTERVAL + " Minimum Distance: " + LOCATION_UPDATE_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}