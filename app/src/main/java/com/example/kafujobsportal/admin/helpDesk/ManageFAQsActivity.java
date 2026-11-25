package com.example.kafujobsportal.admin.helpDesk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.FAQsAdapter;
import com.example.kafujobsportal.models.FAQsModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageFAQsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ArrayList<FAQsModel> faqsList;
    private FAQsAdapter adapter;
    private DatabaseReference faqsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_faqs);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Views
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        faqsList = new ArrayList<>();
        adapter = new FAQsAdapter(this, faqsList);
        recyclerView.setAdapter(adapter);

        // Firebase Reference
        faqsRef = Utils.getDatabaseReference(Constants.FAQ_DB);

        // Load FAQs from Firebase
        loadFAQs();

        // Button to Create New FAQ
        findViewById(R.id.create).setOnClickListener(v ->
                startActivity(new Intent(this, CreateFAQsActivity.class)));
    }

    private void loadFAQs() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        faqsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                faqsList.clear();
                for (DataSnapshot faqSnapshot : snapshot.getChildren()) {
                    FAQsModel faq = faqSnapshot.getValue(FAQsModel.class);
                    if (faq != null) {
                        faqsList.add(faq);
                    }
                }
                adapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageFAQsActivity.this, "Failed to load FAQs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
