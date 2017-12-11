package com.outrun.outrun;

import java.util.ArrayList;
import com.google.android.gms.maps.model.LatLng;

public class Course {
    ArrayList<LatLng> points;

    public Course(ArrayList<LatLng> points) {
        this.points = points;
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



}
