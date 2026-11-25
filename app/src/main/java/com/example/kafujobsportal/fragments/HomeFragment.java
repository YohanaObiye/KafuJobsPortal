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
import com.example.kafujobsportal.adapters.JobAdapterUser;
import com.example.kafujobsportal.models.JobModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView welcomeText;
    private RecyclerView recentJobsRecyclerView;
    private JobAdapterUser jobAdapter;
    private List<JobModel> jobList;

    private FirebaseAuth auth;
    private DatabaseReference jobsRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        jobsRef = FirebaseDatabase.getInstance().getReference("jobs");

        // Initialize UI Components
        welcomeText = view.findViewById(R.id.welcomeText);
        recentJobsRecyclerView = view.findViewById(R.id.recentJobsRecyclerView);

        // Set Welcome Message
        if (auth.getCurrentUser() != null) {
            String userName = auth.getCurrentUser().getDisplayName();
            welcomeText.setText("Welcome, " + (userName != null ? userName : "User"));
        }

        // Set up RecyclerView
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapterUser(getContext(), jobList);
        recentJobsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentJobsRecyclerView.setAdapter(jobAdapter);

        // Fetch Jobs from Firebase Realtime Database
        fetchJobs();

        return view;
    }

    private void fetchJobs() {
        jobsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobList.clear();
                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    JobModel job = jobSnapshot.getValue(JobModel.class);
                    if (job != null) {
                        jobList.add(job);
                    }
                }
                jobAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load jobs!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
