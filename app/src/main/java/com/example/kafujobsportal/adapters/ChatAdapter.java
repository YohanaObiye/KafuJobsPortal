package com.example.kafujobsportal.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kafujobsportal.R;
import com.example.kafujobsportal.models.ChatModel;
import com.example.kafujobsportal.models.UserModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final ArrayList<ChatModel> chatList;
    private final Context context;
    private final String userId;  // Get current user ID from Firebase Auth

    public ChatAdapter(Context context, ArrayList<ChatModel> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel chat = chatList.get(position);
        String senderId = chat.getSenderId();

        if (senderId != null && senderId.equals(userId)) {
            // Current user message styling
            holder.messageTextView.setBackgroundResource(R.drawable.bg_patient_message);
            holder.messageTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.linearLayout.setGravity(Gravity.END);
            holder.senderProfile.setVisibility(View.VISIBLE);
            holder.receiverProfile.setVisibility(View.GONE);
            getImage(senderId, holder.senderProfile);
        } else {
            // Other user's message styling
            holder.messageTextView.setBackgroundResource(R.drawable.bg_doctor_message);
            holder.messageTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.linearLayout.setGravity(Gravity.START);
            holder.senderProfile.setVisibility(View.GONE);
            holder.receiverProfile.setVisibility(View.VISIBLE);
            getImage(senderId, holder.receiverProfile);
        }

        holder.messageTextView.setText(chat.getMessage());
    }

    private void getImage(String userId, CircleImageView profileImage) {
        Utils.getDatabaseReference(Constants.USERS_DB).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model != null && model.getProfileImage() != null && !model.getProfileImage().isEmpty()) {
                        Glide.with(context)
                                .load(model.getProfileImage())
                                .placeholder(R.drawable.profile) // Placeholder image
                                .error(R.drawable.profile) // Error fallback image
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, dateTextView;
        LinearLayout linearLayout;
        CircleImageView receiverProfile, senderProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            receiverProfile = itemView.findViewById(R.id.receiver_profile);
            senderProfile = itemView.findViewById(R.id.sender_profile);
        }
    }
}
