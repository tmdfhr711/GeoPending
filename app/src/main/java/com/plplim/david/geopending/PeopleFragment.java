package com.plplim.david.geopending;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleFragment extends Fragment {
    private final String TAG = "PEOPLE FRAGMENT";

    private RecyclerView recyclerView;
    private UsersListAdapter usersListAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private List<Users> usersList;

    public PeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.peopleRecyclerView);
        firebaseFirestore = FirebaseFirestore.getInstance();

        usersList = new ArrayList<>();
        usersListAdapter = new UsersListAdapter(container.getContext(), usersList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(usersListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));

        FirebaseFirestore.getInstance().collection("Users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.e(TAG, "FirebaseStore CALLBACK");
                        if (e != null) {
                            Log.d(TAG, "Error : " + e.getMessage());
                        }
                        if (firebaseAuth == null) {
                            return;
                        }

                        //usersList.clear();
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String user_id = doc.getDocument().getId();
                                if (!user_id.equals(firebaseAuth.getCurrentUser().getUid())) {
                                    Users users = doc.getDocument().toObject(Users.class).withId(user_id);
                                    usersList.add(users);
                                }



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

        return view;
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

}
