package com.example.kafujobsportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kafujobsportal.R;
import com.example.kafujobsportal.admin.ChatActivity;
import com.example.kafujobsportal.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private final Context context;
    private final ArrayList<UserModel> usersList;

    public UsersAdapter(Context context, ArrayList<UserModel> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = usersList.get(position);
        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole());
        holder.phone.setText(user.getPhone());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.createdAt.setText(sdf.format(user.getCreatedAt()));

        // Load profile image using Glide
        Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(R.drawable.profile)  // Default image
                .into(holder.profileImage);

        holder.card.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", user.getId());
            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username, email, role, phone, createdAt;
        LinearLayout card;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
            role = itemView.findViewById(R.id.role);
            phone = itemView.findViewById(R.id.phone);
            card = itemView.findViewById(R.id.holder);
            createdAt = itemView.findViewById(R.id.created_at);
        }
    }
}
