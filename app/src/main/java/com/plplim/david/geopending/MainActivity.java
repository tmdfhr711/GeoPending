package com.plplim.david.geopending;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
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


        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(usersListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainBottomnavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_people:
                        Toast.makeText(MainActivity.this, "people click", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_map:
                        Toast.makeText(MainActivity.this, "map click", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_account:
                        Toast.makeText(MainActivity.this, "account click", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        FirebaseFirestore.getInstance().collection("Users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.e(TAG, "FirebaseStore CALLBACK");
                        if (e != null) {
                            Log.d(TAG, "Error : " + e.getMessage());
                        }

                        //usersList.clear();
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String user_id = doc.getDocument().getId();
                                Users users = doc.getDocument().toObject(Users.class).withId(user_id);
                                usersList.add(users);


                            } else if (doc.getType() == DocumentChange.Type.MODIFIED) {
                                String userid = doc.getDocument().getId();
                                Users get_user = doc.getDocument().toObject(Users.class).withId(userid);
                                modifyItem(userid, get_user);
                            } else if (doc.getType() == DocumentChange.Type.REMOVED) {
                                deleteItem(doc.getDocument().getId());
                            }

                        }
                        usersListAdapter.notifyDataSetChanged();
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
}
