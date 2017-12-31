package com.outrun.outrun;


import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LeaderboardListEntry {
    public LeaderboardEntry entry;
    public String name;
    public Uri photo;
    public String time;

    public LeaderboardListEntry(LeaderboardEntry entry) {
        this.entry = entry;
        this.time = entry.timeToString();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = mDatabase.child("users").child(entry.userUid);
        if(reference == null) this.name = "HI";
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               setName((String) dataSnapshot.child("name").getValue());
               setPhoto(Uri.parse((String) dataSnapshot.child("photo").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setPhoto(Uri photo) {
        this.photo = photo;
    }
}
