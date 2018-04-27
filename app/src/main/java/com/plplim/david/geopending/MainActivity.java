package com.plplim.david.geopending;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity Firebase";

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;

    private BottomNavigationView bottomNavigationView;

    private PeopleFragment peopleFragment;
    private MapFragment mapFragment;
    private AccountFragment accountFragment;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQEUST_CODE = 1234;

    private  void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this,FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "getLocationPermission: granted");
                replaceFragment(mapFragment);
                getSupportActionBar().setTitle("Map");
            } else {
                Log.d(TAG, "getLocationPermission: coarselocation denied");
                ActivityCompat.requestPermissions(this,permissions, LOCATION_PERMISSION_REQEUST_CODE);
            }
        } else {
            Log.d(TAG, "getLocationPermission: findlocation denied");
            ActivityCompat.requestPermissions(this,permissions, LOCATION_PERMISSION_REQEUST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");

        switch (requestCode) {
            case LOCATION_PERMISSION_REQEUST_CODE : {
                if (grantResults.length > 0) {
                    for(int i = 0; i< grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //initialize our map
                    replaceFragment(mapFragment);
                    getSupportActionBar().setTitle("Map");
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setTitle("Geo Pending");

        peopleFragment = new PeopleFragment();
        mapFragment = new MapFragment();
        accountFragment = new AccountFragment();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainBottomnavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_people:
                        replaceFragment(peopleFragment);
                        getSupportActionBar().setTitle("People");
                        return true;
                    case R.id.action_map:
                        getLocationPermission();
                        return true;
                    case R.id.action_account:
                        replaceFragment(accountFragment);
                        getSupportActionBar().setTitle("Account");
                        return true;
                    default :
                        return false;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //firebaseFirestore = null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            sendToLogin();
        }
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_logout_button:
                logOut();
                return true;

            case R.id.action_settings_button:
                Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(settingsIntent);

                return true;
            default:
                return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}
