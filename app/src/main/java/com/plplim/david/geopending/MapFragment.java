package com.plplim.david.geopending;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapFragment";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQEUST_CODE = 1234;
    private static final int DEFAULT_ZOOM = 15;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private final int MY_LOCATION_REQUEST_CODE = 1;
    private MapView mapView;
    private GoogleMap mGoogleMap = null;

    private View rootView;

    private Marker ruleMarker;
    private Marker userMarker;
    private Circle ruleCircle;

    private TrakingModel trakingModel = null;

    private FirebaseAuth trakingAuth;
    private FirebaseFirestore trakingFirestore;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.getMapAsync(this);

        return rootView;
    }

    public void getGeoPendingFromFirebase() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();

        //내가 현재 트레킹 하고 있는 방을 찾아감
        firebaseFirestore.collection("TrakingRooms").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    trakingModel = task.getResult().toObject(TrakingModel.class);
                    //트레킹룸 정보를 이용해서 트레킹 시작하기
                    //트래킹 위치 셋팅
                    if (trakingModel != null) {
                        if (trakingModel.isDestinationCheck()) {
                            //상대방이 허락했을 떄
                            setRuleInit(trakingModel.getRuleLat(), trakingModel.getRuleLong(), trakingModel.getRuleRadius());
                            startTraking();
                        } else {
                            //상대방이 허락하지 않았을 때
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Geo Pending")
                                    .setMessage("상대방이 트래킹을 허용하지 않아 지도에 보이지 않습니다.")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            return;
                                        }
                                    }).show();
                        }
                    } else {
                        //상대방이 허락하지 않았을 때
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Geo Pending")
                                .setMessage("트래킹을 트래킹 하고 있는 상대가 없습니다")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                }).show();
                    }

                } else {
                    //다이얼로그로 트래킹 할 대상이 없다고 띄워주고 마크 아무대나 찍기
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Geo Pending")
                            .setMessage("트래킹 정보를 불러오는데 오류가 발생하였습니다")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            }).show();
                    goToLocationZoom(39.008224, -76.8984527,DEFAULT_ZOOM);
                }
            }
        });
    }

    public void setRuleInit(double lat,double lon, double radius) {
        if (ruleMarker != null) {
            //removeRuleEverything();
        }
        MarkerOptions options = new MarkerOptions()
                .title("기준위치")
                .position(new LatLng(lat, lon));
        ruleMarker = mGoogleMap.addMarker(options);

        ruleCircle = drawCircle(new LatLng(lat, lon),radius);
    }
    private Circle drawCircle(LatLng latLng, double radius) {

        CircleOptions options = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .fillColor(0x33ff0000)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);
        return mGoogleMap.addCircle(options);
    }

    private void removeRuleEverything() {
        ruleMarker.remove();
        ruleMarker = null;
        ruleCircle.remove();
        ruleCircle = null;
    }

    private void startTraking() {
        trakingAuth = FirebaseAuth.getInstance();
        trakingFirestore = FirebaseFirestore.getInstance();

        trakingFirestore.collection("TrakingRooms").
                whereEqualTo("uid",trakingAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            TrakingModel trakingModel = dc.getDocument().toObject(TrakingModel.class);
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(TAG, "onEvent: Added");
                                    setUserMarker(trakingModel.getDestinationLatitude(), trakingModel.getDestinationLongitude());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "onEvent: Modified");
                                    setUserMarker(trakingModel.getDestinationLatitude(), trakingModel.getDestinationLongitude());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                    break;
                            }
                        }
                    }
                });
    }

    private void setUserMarker(double lat, double lon) {
        if (userMarker != null) {
            userMarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .position(new LatLng(lat, lon));
        userMarker = mGoogleMap.addMarker(options);
        goToLocationZoom(lat, lon, DEFAULT_ZOOM);
    }

    private void goToLocationZoom(double lat, double lon, int zoom) {
        LatLng ll = new LatLng(lat, lon);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        Toast.makeText(getContext(), "map is ready", Toast.LENGTH_SHORT).show();
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        getGeoPendingFromFirebase();

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //getDeviceLocation();
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQEUST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    //initMap();
                }
                break;
            }
            case MY_LOCATION_REQUEST_CODE: {
                Log.d(TAG, "onRequestPermissionsResult: MY_LOCATION_REQUEST_CODE");
            }
        }
    }
}
