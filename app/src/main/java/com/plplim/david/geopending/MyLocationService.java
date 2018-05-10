package com.plplim.david.geopending;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MyLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String MESSAGE_KEY = "locationEnable";
    private static final String TAG = "MyLocationService";


    private ServiceThread thread;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private boolean mRequestingLocationUpdates = false;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public MyLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        buildGoogleApiClient();
        createLocationRequest();

        //tooglePeriodicLocationUdates();p
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: ");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Log.d(TAG, "displayLocation: " + String.valueOf(latitude) + " , " + String.valueOf(longitude));
            sendLocationToFirestore(latitude, longitude);
        } else {
            Log.d(TAG, "displayLocation: " + "Couldn't get the location. Make sure location is enable on the device");
        }
    }

    public void tooglePeriodicLocationUdates() {
        if (!mRequestingLocationUpdates) {
            //button.setText("Stop location update");
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        } else {
            //button.setText("Start location update");
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
    }

    private void sendLocationToFirestore(final double latitude, final double longitude) {
        //나와 연동되어 있는 걸 찾아야함
        firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("TrakingRooms")
                .whereEqualTo("destinationUid", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        TrakingModel trakingModel = null;
                        if (task.isSuccessful()) {
                            //내가 트래킹 당하고 있는 데이터베이스를 가져옴
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                trakingModel = document.toObject(TrakingModel.class);
                                Log.d(TAG, "onComplete: " + trakingModel.getUid() + " , " + trakingModel.getDestinationUid());
                            }
                            //내가 트래킹 당하고 있는 데이터베이스 정보를 가져와서 정보를 update시킴
                            if (trakingModel != null) {
                                trakingModel.setDestinationLatitude(latitude);
                                trakingModel.setDestinationLongitude(longitude);
                                final TrakingModel copyTraking = trakingModel;
                                firebaseFirestore.collection("TrakingRooms").document(trakingModel.getUid()).set(trakingModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "onComplete: success");
                                        if (task.isSuccessful()) {
                                            if (copyTraking.isDestinationCheck()) {
                                                checkTraking(copyTraking);
                                            }

                                        }
                                    }
                                });
                            }

                        }
                    }
                });
    }

    private void checkTraking(TrakingModel trakingModel) {
        //트래킹허용
        Location ruleLocation = new Location("Rule");
        Location userLocation = new Location("User");
        ruleLocation.setLatitude(trakingModel.getRuleLat());
        ruleLocation.setLongitude(trakingModel.getRuleLong());
        userLocation.setLatitude(trakingModel.getDestinationLatitude());
        userLocation.setLongitude(trakingModel.getDestinationLongitude());

        double distance = (double) ruleLocation.distanceTo(userLocation);

        Log.d(TAG, "checkTraking: distance : " + String.valueOf(distance) + "\nruleRadius : " + String.valueOf(trakingModel.getRuleRadius()));
        if (trakingModel.getRuleRadius() < distance) {
            //반경을 벗어났을 때
            sendNotification(trakingModel.getUid());
        }
    }

    private void sendNotification(String uid) {
        //상대방에게 알림 보내기
        Log.d(TAG, "sendNotification: 상대방이 위치를 벗어났습니다");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
        /*if (mRequestingLocationUpdates) {

        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }
}
