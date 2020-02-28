package com.example.findmeuv.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class LocationService extends Service implements LocationListener {

    private final static String DEBUG_TAG = "DebugLog";
    private final static int LOCATION_INTERVAL = 0;
    private final static float LOCATION_DISTANCE = 0;

    private LocationManager locationManager;

    private void initLocationManager() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, this);
            }

        } else {

        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG_TAG, "LocationService->IBinder()");
        return null;
    }


    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, "LocationService->onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG_TAG, "LocationService->onStartCommand()");

        initLocationManager();

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "LocationService->onDestroy()");
        super.onDestroy();
    }

    // Location Listener

    @Override
    public void onLocationChanged(Location location) {
        Log.d(DEBUG_TAG, "LocationService->onLocationChanged()");

        Toast.makeText(getApplicationContext(), "LAT: "+ String.valueOf(location.getLatitude())+" LNG: " + String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(DEBUG_TAG, "LocationService->onStatusChanged()");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(DEBUG_TAG, "LocationService->onProviderEnabled()");
        Toast.makeText(getApplicationContext(), "GPS is disabled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(DEBUG_TAG, "LocationService->onProviderDisabled()");
        Toast.makeText(getApplicationContext(), "GPS is enabled.", Toast.LENGTH_SHORT).show();
    }
}
