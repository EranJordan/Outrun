package com.outrun.outrun;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.TimeUnit;

public class RunCourseActivity extends AppCompatActivity
            implements
            OnMapReadyCallback,
            ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener, View.OnClickListener {

    TextView timeTextView;
    public FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    private LocationManager locationManager;
    public final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mPermissionDenied = false;
    private String provider;
    private long timeStart;
    long time = 0;
    private Handler counterHandler = new Handler();
    //TODO: use parcel // serialize userUid and course object to this activity so it can upload the entry to the correct location in the database.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        timeStart = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_course);
        setContentView(com.outrun.outrun.R.layout.activity_create_course);
        findViewById(R.id.done_button).setOnClickListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //course = new Course();
        timeTextView = findViewById(R.id.time_textView);
        updateTime();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.done_button) {
            uploadLeaderboardEntry();
        }
    }
    private void updateTime() {
        counterHandler.postDelayed(textViewChanger, 1000);
    }

    private Runnable textViewChanger = new Runnable(){
        public void run() {
            timeTextView.setText(timeToString(time));
            time +=1000;
            updateTime();
        }
    };

    private void uploadLeaderboardEntry() {
        long timeElapsed = System.currentTimeMillis() - timeStart; //because I don't trust android to count correctly
        //TODO
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableLocation();
    }
    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // Define the criteria how to select the location provider -> use
            // default
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
            Location pos = locationManager.getLastKnownLocation(provider);
            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(pos.getLatitude(), pos.getLongitude()) , 17.0f) );
        }
    }

    public String timeToString(long time) {
        long millis = time;
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append("h:");
        sb.append(minutes);
        sb.append("m:");
        sb.append(seconds);
        sb.append("s");
        return(sb.toString());
    }
}
