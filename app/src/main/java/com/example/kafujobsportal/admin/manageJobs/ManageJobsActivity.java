package com.example.kafujobsportal.admin.manageJobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.JobAdapter;
import com.example.kafujobsportal.models.JobModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class ManageJobsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private ArrayList<JobModel> jobList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_jobs);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobList);
        recyclerView.setAdapter(jobAdapter);

        findViewById(R.id.create).setOnClickListener(v ->
                startActivity(new Intent(
                        ManageJobsActivity.this,
                        CreateJobActivity.class)));

        fetchJobsFromFirebase();
    }

    private void fetchJobsFromFirebase() {
        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading jobs...");
        progressDialog.setCancelable(false); // Prevent dismissal
        progressDialog.show();

        Utils.getDatabaseReference(Constants.JOB_DB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jobList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    JobModel job = snapshot.getValue(JobModel.class);
                    if (job != null) {
                        jobList.add(job);
                    }
                }
                Collections.sort(jobList, (j1, j2) -> Long.compare(j2.getCreatedAt(), j1.getCreatedAt()));
                jobAdapter.notifyDataSetChanged();

                // Dismiss ProgressDialog when loading is complete
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ManageJobsActivity.this, "Failed to fetch jobs: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                // Dismiss ProgressDialog in case of an error
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }
}