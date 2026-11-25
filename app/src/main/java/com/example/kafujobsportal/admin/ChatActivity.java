package com.example.kafujobsportal.admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.ChatAdapter;
import com.example.kafujobsportal.models.ChatModel;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    TextInputEditText edMessage;
    ArrayList<ChatModel> chatList = new ArrayList<>();
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    String senderId, receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        edMessage = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(ChatActivity.this, chatList);
        recyclerView.setAdapter(chatAdapter);

        senderId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        String receivedUserId = getIntent().getStringExtra("userId");
        receiverId = (receivedUserId != null && !receivedUserId.isEmpty()) ? receivedUserId : "f8QJb4WosgdqyvA5k3Ewt7VLFkI2";

        findViewById(R.id.send).setOnClickListener(v -> {
            String message = edMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                sendMessage(message);
                edMessage.setText("");
            }
        });

        loadChats();
    }

    private void loadChats() {
        if (senderId == null || receiverId == null) return;

        String roomId1 = senderId + receiverId;
        String roomId2 = receiverId + senderId;

        Utils.getDatabaseReference("chats").orderByChild("currentDate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String roomId = snapshot.getKey();
                    if (roomId != null && (roomId.equals(roomId1) || roomId.equals(roomId2))) {
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            ChatModel chat = messageSnapshot.getValue(ChatModel.class);
                            if (chat != null) chatList.add(chat);
                        }
                    }
                }

                // Sort messages by date
                Collections.sort(chatList, (chat1, chat2) -> chat1.getCurrentDate().compareTo(chat2.getCurrentDate()));

                recyclerView.post(() -> recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1));
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {
        if (senderId == null || receiverId == null) {
            Toast.makeText(this, "User authentication failed", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending...");
        progressDialog.show();

        String currentDate = getCurrentTime();
        String chatId = Utils.getDatabaseReference("chats").push().getKey();
        String roomId = senderId + receiverId;

        ChatModel chat = new ChatModel(chatId, roomId, senderId, receiverId, message, currentDate);
        Utils.getDatabaseReference("chats").child(roomId).child(chatId).setValue(chat)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Failed to Send", Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
