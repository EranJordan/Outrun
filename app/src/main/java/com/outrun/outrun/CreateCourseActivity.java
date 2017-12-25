package com.outrun.outrun;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateCourseActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener, View.OnClickListener {

    private GoogleMap mMap;
    public final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;
    private String provider;
    private ArrayList<MarkerOptions> markers;
    private Course course;
    private DatabaseReference mDatabase;
    private DownloadUtils downloadUtils = new DownloadUtils();
    TextView distanceTextView;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.outrun.outrun.R.layout.activity_create_course);
        findViewById(R.id.done_button).setOnClickListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markers = new ArrayList<>();
        course = new Course();
        distanceTextView = findViewById(R.id.distance_textView);
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
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableLocation();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
                MarkerOptions marker = new MarkerOptions().position(
                        point).title("Course Marker " + markers.size());
                mMap.addMarker(marker);
                markers.add(marker);
                course.addPoint(point);
                if (course.getSize() > 1) {

                    PolylineOptions polyLine = new PolylineOptions().color(
                            Color.BLUE).width((float) 7.0);
                    polyLine.add(point);
                    LatLng previousPoint = course.get(course.getSize() - 2);
                    polyLine.add(previousPoint);
                    DownloadTask downloadTask = new DownloadTask();
                    String url = downloadUtils.getDirectionsUrl(point, previousPoint);
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
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

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.done_button) {
            finishCourse();
        }
    }

    private void finishCourse() {
        if(course.getSize() < 2) {
            Toast.makeText(this, "Please choose at least 2 points", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Intent mapIntent = new Intent(this, MapsActivity.class);

        builder.setMessage("Select course type")
                .setTitle("Finish course");
        builder.setPositiveButton("A -> B", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                uploadCourseToDatabase();
                Toast.makeText(CreateCourseActivity.this, "Course Created", Toast.LENGTH_SHORT).show();
                startActivity(mapIntent);
            }
        });
        builder.setNegativeButton("A -> A", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                PolylineOptions polyLine = new PolylineOptions().color(
                        Color.BLUE).width((float) 7.0);
                LatLng point = course.get(0);
                course.addPoint(point);
                polyLine.add(point);
                LatLng previousPoint = course.get(course.getSize() - 2);
                polyLine.add(previousPoint);
                String url = downloadUtils.getDirectionsUrl(point, previousPoint);
                DownloadTask downloadTask = new DownloadTask();
                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
                uploadCourseToDatabase();
                Toast.makeText(CreateCourseActivity.this, "Course Created", Toast.LENGTH_SHORT).show();
                startActivity(mapIntent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void uploadCourseToDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        final String userUid = mAuth.getCurrentUser().getUid();
        course.userUid = userUid;
        DatabaseReference courseRef =  mDatabase.child("users").child(userUid).push();
        courseRef.setValue(course);
    }

    public Context getActivity() {
        return this;
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUtils.downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                    if(j > 0) {
                        course.updateDist(position, (LatLng)points.get(j-1));
                    }
                }
             //   course.points.addAll(course.getSize() - 1, points); //add all intermediate points between the 2 points the user clicked on
                lineOptions.addAll(points);
            }

// Drawing polyline in the Google Map for the i-th route
            if(points != null) {
                if(points.size()!= 0) {
                    mMap.addPolyline(lineOptions);  //this line is bad
                    distanceTextView.setText("Distance: " + course.getDistance() + "m");

                }
            }
        }
    }
}
