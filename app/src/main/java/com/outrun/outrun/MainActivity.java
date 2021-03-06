package com.outrun.outrun;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    public final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final static String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    System.exit(0); 
                }
                return;
            }
        }
    }

    /* (non-Javadoc)
   * @see android.app.Activity#onDestroy()
   */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    /* (non-Javadoc)
    * @see android.app.Activity#onPause()
    */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onRestart()
    */
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onResume()
    */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onStart()
    */
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        redirectUI(account);
    }

    protected void redirectUI(GoogleSignInAccount account) {
        if(account == null) {
            Intent signIn = new Intent(this, GoogleSignInActivity.class);
            startActivity(signIn);
        }
        else{
            Intent mainPage = new Intent(this, ProfileActivity.class);
            startActivity(mainPage);
        }
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onStop()
    */
    @Override
    protected void onStop() {
        super.onStop();
    }
}
