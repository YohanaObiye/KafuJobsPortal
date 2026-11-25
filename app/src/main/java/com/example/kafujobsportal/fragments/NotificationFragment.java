package com.example.kafujobsportal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.NotificationAdapter;
import com.example.kafujobsportal.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationList;
    private TextView noNotificationsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        noNotificationsText = view.findViewById(R.id.no_notifications_text);

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        fetchNotifications();
        return view;
    }

    private void fetchNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("notifications")
                .orderByChild("receiverId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            NotificationModel notification = data.getValue(NotificationModel.class);
                            if (notification != null) {
                                notificationList.add(notification);
                            }
                        }
                        // Sort notifications (latest first)
                        Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                        // Show or hide "No notifications" text
                        if (notificationList.isEmpty()) {
                            noNotificationsText.setVisibility(View.VISIBLE);
                            notificationsRecyclerView.setVisibility(View.GONE);
                        } else {
                            noNotificationsText.setVisibility(View.GONE);
                            notificationsRecyclerView.setVisibility(View.VISIBLE);
                        }

                        notificationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to fetch notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
