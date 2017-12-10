package com.outrun.outrun;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class ProfileActivity extends AppCompatActivity  implements View.OnClickListener {
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.map_button).setOnClickListener(this);
        findViewById(R.id.signout_button).setOnClickListener(this);
        name = account.getDisplayName();
        Uri profileImage = account.getPhotoUrl();
        Glide.with(this).load(profileImage).into((ImageView) findViewById(R.id.profile_imageView));
        final TextView nameTextView = findViewById(R.id.name_textView);
        nameTextView.setText(name);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.map_button:
                Intent mapIntent = new Intent(this, MapsActivity.class);
                startActivity(mapIntent);
                break;
            case R.id.signout_button:
               Intent signOut = new Intent(this, GoogleSignInActivity.class);
               startActivity(signOut);
               break;
         }
    }
}
