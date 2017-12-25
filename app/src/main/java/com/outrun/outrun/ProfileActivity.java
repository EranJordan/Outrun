package com.outrun.outrun;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class ProfileActivity extends AppCompatActivity  implements View.OnClickListener {
    String name;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.map_button).setOnClickListener(this);
        findViewById(R.id.signout_button).setOnClickListener(this);
        findViewById(R.id.run_text).setOnClickListener(this);
        findViewById(R.id.signout_text).setOnClickListener(this);
        name = account.getDisplayName();
        Uri profileImage = account.getPhotoUrl();
        Glide.with(this).load(profileImage).into((ImageView) findViewById(R.id.profile_imageView));
        final TextView nameTextView = findViewById(R.id.name_textView);
        nameTextView.setText(name);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.map_button:
            case R.id.run_text:
                Intent mapIntent = new Intent(this, MapsActivity.class);
                startActivity(mapIntent);
                break;
            case R.id.signout_button:
            case R.id.signout_text:
               Intent signOut = new Intent(this, GoogleSignInActivity.class);
               startActivity(signOut);
               break;
         }
    }
}
