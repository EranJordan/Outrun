package com.outrun.outrun;


import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

public class DatabaseUtils {

    public Course getCourseFromDatabase(DataSnapshot ref) {
        Course course = new Course();
        course.distance = (long) ref.child("distance").getValue();
        course.userUid = (String) ref.child("userUid").getValue();
        long size = (long) ref.child("size").getValue();
        long leaderboardSize = (long) ref.child("leaderboardSize").getValue();
        DataSnapshot points = ref.child("points");
        for(int i = 0; i < size; i++) {
            course.addPoint(getLatLngFromDatabase(points, String.valueOf(i)));
        }
        DataSnapshot leaderboard = ref.child("leaderboard");
        for(int i = 0; i < leaderboardSize; i++) {
            course.leaderboard.add(getEntryFromDatabase(leaderboard, String.valueOf(i)));
        }
        return course;
    }
    public LatLng getLatLngFromDatabase(DataSnapshot ref, String i) { //gets "points" reference and index of point
        DataSnapshot point = ref.child(i);
        double lat = (double) point.child("latitude").getValue();
        double lng = (double) point.child("longitude").getValue();
        return new LatLng(lat, lng);
    }

    public LeaderboardEntry getEntryFromDatabase(DataSnapshot ref, String i) {
        DataSnapshot entry = ref.child(i);
        LeaderboardEntry x = new LeaderboardEntry();
        long time = (long) entry.child("time").getValue();
        String userUid = entry.child("userUid").getValue().toString();
        x.time = time;
        x.userUid = userUid;
        return x;
    }
}
