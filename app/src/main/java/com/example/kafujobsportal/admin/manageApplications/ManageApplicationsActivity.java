package com.example.kafujobsportal.admin.manageApplications;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.ApplicationAdapter;
import com.example.kafujobsportal.models.ApplicationModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ManageApplicationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApplicationAdapter applicationAdapter;
    private ArrayList<ApplicationModel> applicationList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_applications);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        applicationList = new ArrayList<>();
        applicationAdapter = new ApplicationAdapter(this, applicationList);
        recyclerView.setAdapter(applicationAdapter);

        fetchApplicationsFromFirebase();
    }

    private void fetchApplicationsFromFirebase() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading applications...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Toast.makeText(this, "zreached here", Toast.LENGTH_SHORT).show();

        Utils.getDatabaseReference(Constants.APPLICATIONS_DB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                applicationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ApplicationModel application = snapshot.getValue(ApplicationModel.class);
                    Log.d("TestingError1", application.getApplicationId());
                    if (application != null) {
                        applicationList.add(application);
                    }
                }
                Collections.sort(applicationList, (a1, a2) -> Long.compare(a2.getCreatedAt(), a1.getCreatedAt()));
                applicationAdapter.notifyDataSetChanged();

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ManageApplicationsActivity.this, "Failed to fetch applications: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
