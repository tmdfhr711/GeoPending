package com.plplim.david.geopending;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.xw.repo.BubbleSeekBar;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class GeoPickFragment extends Fragment implements OnConnectionFailedListener, OnMapReadyCallback {
    private static final String TAG = "PlacePickerSample";

    private GoogleApiClient mGoogleApiClient;
    private View rootView;


    /**
     * Request code passed to the PlacePicker intent to identify its result when it returns.
     */
    private static final int REQUEST_PLACE_PICKER = 1;

    private String uid = null;
    private String destinationUid = null;
    private double latitude = 37.56, longitude = 126.97;
    private int radius = 500;
    private String address = null;

    private MapView mapView;
    private TextView addressText;
    private BubbleSeekBar radiusSeekbar;
    private Button saveButton;
    private CircleOptions circleOptions;
    private GoogleMap nMap;
    public FirebaseAuth firebaseAuth;
    public FirebaseFirestore firebaseFirestore;

    private String getDestinationToken;
    private Users destinationUser = null;

    public GeoPickFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        if (getArguments() != null) {
            uid = getArguments().getString("uid");
            destinationUid = getArguments().getString("destinationUid");
        }

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_geo_pick, container, false);

        mapView = (MapView) rootView.findViewById(R.id.geopick_mapview);
        addressText = (TextView) rootView.findViewById(R.id.geopick_textview_address);
        radiusSeekbar = (BubbleSeekBar) rootView.findViewById(R.id.geopick_seekbar_radius);
        saveButton = (Button) rootView.findViewById(R.id.geopick_button_save);
        mapView.getMapAsync(this);

        getTokenFromFirebase(destinationUid);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth = FirebaseAuth.getInstance();
                String uid = firebaseAuth.getCurrentUser().getUid();

                TrakingModel trakingModel = new TrakingModel();
                trakingModel.setUid(uid);
                trakingModel.setDestinationUid(destinationUid);
                trakingModel.setRuleLat(latitude);
                trakingModel.setRuleLong(longitude);
                trakingModel.setRuleRadius(radius);
                trakingModel.setTo(FirebaseInstanceId.getInstance().getToken());
                firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseFirestore.collection("TrakingRooms").document(uid).set(trakingModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //상대방에게 푸쉬 메세지 보내기
                            Toast.makeText(getContext(), "트래킹 요청을 보냈습니다", Toast.LENGTH_SHORT).show();
                            FragmentTransaction fragmentTransaction = ((AppCompatActivity)getContext()).getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.main_container, new PeopleFragment());
                            fragmentTransaction.commit();
                            if (!getDestinationToken.equals("")) {

                            }
                            if (destinationUser != null) {
                                sendGcm(destinationUser);
                            }
                        } else {
                            //상대방에게 요청 실패
                        }
                    }
                });
            }
        });
        radiusSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                radius = progress;
                showPickerOnMap(latitude,longitude,radius);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
        });
        try {
            //장소선택기 실행
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent intent = builder.build(getActivity());
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
        return rootView;
    }

    private void sendGcm(Users user) {
        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();

        notificationModel.to = user.getToken();
        notificationModel.data.title = user.name + "에게 트래킹 요청이 들어왔습니다!";
        notificationModel.data.text = "트래킹 정보를 확인해보세요";
        notificationModel.data.category = "accountFragment";
        notificationModel.data.sound = "default";
        notificationModel.data.priority = "high";

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AIzaSyBFKjMFI7-AV6YAaVTn9ndn8POm-vO19to")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("SendGcm onResponse", response.toString());
            }
        });
    }

    private void getTokenFromFirebase(String uid) {
        Log.d(TAG, "getTokenFromFirebase: start function");
        FirebaseFirestore conn = FirebaseFirestore.getInstance();

        conn.collection("Users").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String token;
                if (task.isSuccessful()) {
                    //Log.d(TAG, "onComplete: " + task.getResult().getData().get("token").toString());
                    getDestinationToken = task.getResult().getData().get("token").toString();
                    destinationUser = task.getResult().toObject(Users.class);
                } else {
                    getDestinationToken = "";
                    destinationUser = null;
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(getContext(), data);
                if (place != null) {
                    String name = place.getName().toString();
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                    address = place.getAddress().toString();
                    //Log.d(TAG, "onActivityResult: " + "이름 : " + name + "\n주소 : " + address + "\n위도, 경도 : " + "(" + latitude + " , " + longitude + ")");
                    //String toastMsg = String.format("Place: %s", place.getName());
                    //Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "onActivityResult: null");
                }

            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        if (mapView != null) {
            mapView.onStart();
        }

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (address != null) {
            addressText.setText(address);
            showPickerOnMap(latitude,longitude,radius);
            saveButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            saveButton.setEnabled(true);
        }
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
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        nMap = googleMap;
        LatLng SEOUL = new LatLng(37.56, 126.97);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("수도");
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //getDeviceLocation();
                return true;
            }
        });
    }

    public void showPickerOnMap(double latitude, double longitude, int radius) {
        nMap.clear();
        LatLng LOCATION = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(LOCATION);
        nMap.addMarker(markerOptions);
        nMap.moveCamera(CameraUpdateFactory.newLatLng(LOCATION));
        nMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        circleOptions = new CircleOptions()
                .center(LOCATION)
                .radius(radius)
                .strokeWidth(2)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(128, 255, 0, 0))
                .clickable(true);

        Circle circle = nMap.addCircle(circleOptions);
    }
}
