package com.csci571hw9.news;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.csci571hw9.news.constant.ConstantPool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private LocationManager locationManager;
    private double lat = 34.0266; // default value is used iff users deny giving location authority
    private double lon = -118.283; // default value is used iff users deny giving location authority
    private String city;
    private String state;
    private int Temperature;
    private String Summary;

    private RequestQueue queue;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("city", city);
                intent.putExtra("state", state);
                intent.putExtra("Temperature", Temperature);
                intent.putExtra("Summary", Summary);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        queue = Volley.newRequestQueue(this);

        boolean checkLocPermission = checkLocationPermission();

        // Already get permission, which means that user has granted location permission to the NewsApp before
        if (checkLocPermission) {
            String locationProvider = null;
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                locationProvider = LocationManager.GPS_PROVIDER;
                Location location = locationManager.getLastKnownLocation(locationProvider);
                lat = location.getLatitude();
                lon = location.getLongitude();
                Log.i("Already get permission", "" + lat);
                Log.i("Already get permission", "" + lon);

                getGeoInfo();
                getWeatherInfo();
            }
        }
    }

    private void getWeatherInfo() {
        StringRequest request = new StringRequest(ConstantPool.WEATHER_REQUEST_LINK + city, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Summary = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    Temperature = (int)(Math.round(jsonObject.getJSONObject("main").getDouble("temp")));

                    handler.sendEmptyMessageDelayed(1, 1000);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        queue.add(request);
    }

    private void getGeoInfo() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        city = addresses.get(0).getLocality();
        state = addresses.get(0).getAdminArea();
    }

    /**
     * @return false: request permission; true: already get permission
     */
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Location Permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // When user opens the NewsApp for the first time, the NewsApp requests the user to grant permission
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        LocationListener locListener = new MyLocationListener();
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
                        String locationProvider = LocationManager.GPS_PROVIDER;
                        Location location = locationManager.getLastKnownLocation(locationProvider);
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        Log.i("Permission was granted", "" + lat);
                        Log.i("Permission was granted", "" + lon);

                        getGeoInfo();
                        getWeatherInfo();
                    }
                } else {
                    // permission was denied, use default values of lat and lon
                    getGeoInfo();
                    getWeatherInfo();
                }
            }
        }
    }

    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) { }

        public void onProviderDisabled(String provider) { }

        public void onProviderEnabled(String provider) { }

        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }
}
