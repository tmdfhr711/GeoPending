package com.plplim.david.geopending;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by OHRok on 2018-04-19.
 */

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {

    public List<Users> usersList;
    public Context context;
    public FirebaseAuth firebaseAuth;
    public FirebaseFirestore firebaseFirestore;

    public UsersListAdapter(Context context,List<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userslist_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RequestOptions placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.default_profile_icon);
        Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(usersList.get(position).getImage()).into(holder.imageview);
        holder.nameText.setText(usersList.get(position).getName());
        holder.groupText.setText(usersList.get(position).getGroup());

        final String destinationUid = usersList.get(position).userId;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "User ID : " + destinationUid, Toast.LENGTH_LONG).show();
                Dialog(destinationUid);
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public CircleImageView imageview;
        public TextView nameText;
        public TextView groupText;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            imageview = (CircleImageView) mView.findViewById(R.id.userslist_image);
            nameText = (TextView) mView.findViewById(R.id.userslist_name);
            groupText = (TextView) mView.findViewById(R.id.userslist_group);
        }
    }

    public void Dialog(final String destinationUid){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Geo Pending");
        builder.setMessage("상대방에게 Geo Pending 수락요청을 보내시겠습니까?");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseAuth = FirebaseAuth.getInstance();
                        String uid = firebaseAuth.getCurrentUser().getUid();

                        TrakingModel trakingModel = new TrakingModel();
                        trakingModel.users.put(uid, true);
                        trakingModel.users.put(destinationUid, false);

                        firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection("TrakingRooms").document(uid).set(trakingModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //상대방에게 트레킹을 요청하는 푸쉬메세지 보내기
                                Toast.makeText(context,"좌측버튼 클릭됨",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context,"취소하였습니다",Toast.LENGTH_SHORT).show();
                        firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection("TrakingRooms")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                                Log.d("GET TRAKING MODELS", documentSnapshot.getId() + " => " + documentSnapshot.getData());
                                            }
                                        } else{
                                            Log.d("GET TRAKING MODELS", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    }
                });
        builder.show();
    }
}
