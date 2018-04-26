package com.plplim.david.geopending;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity Firebase";

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;

    private List<Users> usersList;
    private UsersListAdapter usersListAdapter;

    private BottomNavigationView bottomNavigationView;

    private PeopleFragment peopleFragment;
    private MapFragment mapFragment;
    private AccountFragment accountFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setTitle("Geo Pending");

        usersList = new ArrayList<>();
        usersListAdapter = new UsersListAdapter(MainActivity.this, usersList);


        //recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        firebaseFirestore = FirebaseFirestore.getInstance();

        /*recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(usersListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));*/

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
                        return true;
                    case R.id.action_map:
                        replaceFragment(mapFragment);
                        return true;
                    case R.id.action_account:
                        replaceFragment(accountFragment);
                        return true;
                    default :
                        return false;
                }
            }
        });




    }

    public void deleteItem(String deleteKey) {
        for(int i = 0; i < usersList.size(); i++) {
            //Log.d(TAG, "DELETE_ITEM\n" + "DeleteKey : " + deleteKey + "\nuserListID : " + item.userId);
            if (deleteKey.equals(usersList.get(i).userId)) {
                usersList.remove(i);
            }
        }
    }

    public void modifyItem(String modifyItem, Users users) {
        /*int index = 0;
        for (Users item : usersList) {
            //Log.d(TAG, "MODIFY_ITEM\n" + "DeleteKey : " + modifyItem + "\nuserListID : " + item.userId);
            if (modifyItem.equals(item.userId)) {
                usersList.set(index, users);
            }
            index++;
        }*/

        Log.e("MODIFYITEM", users.getName().toString());
        for(int i = 0; i < usersList.size(); i++) {
            //Log.d(TAG, "DELETE_ITEM\n" + "DeleteKey : " + deleteKey + "\nuserListID : " + item.userId);
            if (modifyItem.equals(usersList.get(i).userId)) {
                usersList.set(i, users);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseFirestore = null;
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
