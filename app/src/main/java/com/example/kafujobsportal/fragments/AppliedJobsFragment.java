package com.example.kafujobsportal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.AppliedJobsAdapter;
import com.example.kafujobsportal.models.ApplicationModel;
import com.example.kafujobsportal.models.JobModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AppliedJobsFragment extends Fragment {

    private RecyclerView appliedJobsRecyclerView;
    private AppliedJobsAdapter appliedJobsAdapter;
    private List<JobModel> appliedJobsList;

    private FirebaseAuth auth;
    private DatabaseReference applicationsRef, jobsRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applied_jobs, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        applicationsRef = FirebaseDatabase.getInstance().getReference("job_applications");
        jobsRef = FirebaseDatabase.getInstance().getReference("jobs");

        // Initialize UI Components
        appliedJobsRecyclerView = view.findViewById(R.id.appliedJobsRecyclerView);
        appliedJobsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize List and Adapter
        appliedJobsList = new ArrayList<>();
        appliedJobsAdapter = new AppliedJobsAdapter(getContext(), appliedJobsList);
        appliedJobsRecyclerView.setAdapter(appliedJobsAdapter);

        // Fetch Applied Jobs
        fetchAppliedJobs();

        return view;
    }

    private void fetchAppliedJobs() {
        String currentUserId = auth.getCurrentUser().getUid();

        applicationsRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appliedJobsList.clear();

                for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                    ApplicationModel application = applicationSnapshot.getValue(ApplicationModel.class);
                    if (application != null) {
                        fetchJobDetails(application.getJobId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch applied jobs!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchJobDetails(String jobId) {
        jobsRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JobModel job = snapshot.getValue(JobModel.class);
                if (job != null) {
                    appliedJobsList.add(job);
                    appliedJobsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch job details!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
