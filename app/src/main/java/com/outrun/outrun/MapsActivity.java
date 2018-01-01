package com.outrun.outrun;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        View.OnClickListener {

    private GoogleMap mMap;
    public final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;
    private String provider;
    public FirebaseAuth mAuth;
    private BitmapDescriptor courseIcon;
    private DownloadUtils downloadUtils = new DownloadUtils();
    public List<Polyline> polylines;
    public CourseTag courseTag;
    private DatabaseUtils databaseUtils;
    ListView listView;
    ArrayList<LeaderboardListEntry> entries;
    private static LeaderboardEntryAdapter adapter;
    private TextView distanceTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        listView = findViewById(R.id.listView);
        findViewById(R.id.course_button).setOnClickListener(this);
        findViewById(R.id.run_button).setOnClickListener(this);
        findViewById(R.id.leaderboard_button).setOnClickListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        courseIcon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_course_round);
        polylines = new ArrayList();
        databaseUtils = new DatabaseUtils();
        mAuth = FirebaseAuth.getInstance();
        distanceTextView = findViewById(R.id.distance_textView2);
    }
//
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableLocation();
        displayCourses();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                mMap.animateCamera( CameraUpdateFactory.newLatLng(marker.getPosition()) );
                marker.showInfoWindow();
                if(polylines.size() > 0) {
                   for(int i = 0; i < polylines.size(); i++) polylines.get(i).remove();
                   polylines.clear();
                }

                courseTag = (CourseTag) marker.getTag();
                Course course = courseTag.course;
                entries = new ArrayList<>();
                for(int i = 0; i < course.getLeaderboardSize(); i++) {
                    entries.add(new LeaderboardListEntry(courseTag.course.leaderboard.get(i)));
                }
                for(int i = 1; i < course.getSize(); i ++) {
                    DownloadTask downloadTask = new DownloadTask();
                    LatLng previousPoint = course.get(i - 1);
                    LatLng point = course.get(i);
                    String url = downloadUtils.getDirectionsUrl(point, previousPoint);
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }
                distanceTextView.setVisibility(View.VISIBLE);
                distanceTextView.setText("Distance: " + course.getDistance() + "m");
                findViewById(R.id.course_button).setVisibility(View.GONE);
                findViewById(R.id.run_button).setVisibility(View.VISIBLE);
                findViewById(R.id.leaderboard_button).setVisibility(View.VISIBLE);
                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(polylines.size() > 0) {
                    for(int i = 0; i < polylines.size(); i++) polylines.get(i).remove();
                    polylines.clear();
                }
                if(findViewById(R.id.listView).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.listView).setVisibility(View.GONE);
                }
                distanceTextView.setVisibility(View.GONE);
                findViewById(R.id.run_button).setVisibility(View.GONE);
                findViewById(R.id.leaderboard_button).setVisibility(View.GONE);
                findViewById(R.id.course_button).setVisibility(View.VISIBLE);
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

    private void displayCourses() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = mDatabase.child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot user : dataSnapshot.getChildren() ){ //for each user
                    for(DataSnapshot course : user.getChildren()) { //go over every course
                        if(course.getKey().equals("name") || course.getKey().equals("photo")) continue;
                        DataSnapshot points = course.child("points");
                        Marker marker = mMap.addMarker(new MarkerOptions().position(databaseUtils.getLatLngFromDatabase(points, "0")).icon(courseIcon).title(makeTitle((String)user.child("name").getValue())));
                        Course curCourse = databaseUtils.getCourseFromDatabase(course);
                        marker.setTag(new CourseTag(curCourse, course.getKey()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onBackPressed()
    {
        if(findViewById(R.id.listView).getVisibility() == View.VISIBLE) {
            findViewById(R.id.listView).setVisibility(View.GONE);
            return;
        }
        Intent intent = new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int j = v.getId();
        switch(j) {
            case R.id.course_button:
                if(polylines.size() > 0) {
                    for(int i = 0; i < polylines.size(); i++) polylines.get(i).remove();
                    polylines.clear();
                }
                Intent createCourseIntent = new Intent(this, CreateCourseActivity.class);
                startActivity(createCourseIntent);
                break;
            case R.id.run_button:

                Intent runCourseIntent = new Intent(this, RunCourseActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("thisUserUid", mAuth.getCurrentUser().getUid());
                mBundle.putString("courseUserUid", this.courseTag.course.userUid);
                mBundle.putString("courseID", this.courseTag.id);
                ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();
                for(int i = 0; i < polylines.size(); i++) {
                   polylineOptions.add(new PolylineOptions().addAll(polylines.get(i).getPoints()));
                }
                mBundle.putParcelableArrayList("polylineOptions", polylineOptions);
                runCourseIntent.putExtra("bundle", mBundle);
                startActivity(runCourseIntent);
                break;
            case R.id.leaderboard_button:
                if(findViewById(R.id.listView).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.listView).setVisibility(View.GONE);
                }
                else showLeaderboards();
                break;
        }
    }

    private void showLeaderboards() {
/*        ArrayList<LeaderboardListEntry> entries = new ArrayList<>();
        for(int i = 0; i < courseTag.course.getLeaderboardSize(); i++) {
            entries.add(new LeaderboardListEntry(courseTag.course.leaderboard.get(i)));
        }*/
        findViewById(R.id.listView).setVisibility(View.VISIBLE);
        adapter = new LeaderboardEntryAdapter(entries, getApplicationContext());
        listView.setAdapter(adapter);
    }

    private String makeTitle(String name) {
        return name.charAt(name.length() - 1) == 's' ? (name + "' course") : (name + "'s course");
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
            ArrayList points;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                polylines.add(mMap.addPolyline(new PolylineOptions().addAll(points)));
            }

        }
    }
}
