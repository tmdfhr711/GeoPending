package com.plplim.david.geopending;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by OHRok on 2018-04-19.
 */

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {

    public List<Users> usersList;
    public Context context;

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

        final String user_id = usersList.get(position).userId;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "User ID : " + user_id, Toast.LENGTH_LONG).show();
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
}
