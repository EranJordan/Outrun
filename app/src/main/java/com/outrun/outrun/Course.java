package com.outrun.outrun;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class Course {
    public ArrayList<LatLng> points;
    public long distance;
    public String userUid;
   // public TreeSet<LeaderboardEntry> leaderboard;

    public Course() {
        this.points = new ArrayList<>();
      //  this.leaderboard = new TreeSet<LeaderboardEntry>(new entryComparator);
        this.distance = 0;
    }

    public ArrayList<LatLng> getPoints(){
        return this.points;
    }


    public LatLng get(int i) {
        return this.points.get(i);
    }

    public int getSize(){
        return this.points.size();
    }

    public void addPoint(LatLng point) {
        points.add(point);
    }


    public void updateDist(LatLng a, LatLng b) {
        distance += distance(a,b);
    }
    public long getDistance() {
        return distance;
    }


    public long distance(LatLng a, LatLng b) {
        long R = 6378100; // meters
        double lat1 = a.latitude;
        double lon1 = a.longitude;
        double lat2 = b.latitude;
        double lon2 = b.longitude;
        double dLat = (lat2-lat1) * Math.PI / 180;
        double dLon = (lon2-lon1) * Math.PI / 180;
        double c = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180 ) * Math.cos(lat2 * Math.PI / 180 ) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double d = 2 * Math.atan2(Math.sqrt(c), Math.sqrt(1-c));
        double e = R * d;
        return (long)e;
    }


    private class entryComparator implements Comparator<LeaderboardEntry> {
        @Override
        public int compare(LeaderboardEntry x, LeaderboardEntry y) {
            return Long.compare(x.time, y.time);
        }
    }
}
