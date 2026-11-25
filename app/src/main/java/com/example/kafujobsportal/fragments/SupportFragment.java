package com.example.kafujobsportal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.FAQsAdapterUser;
import com.example.kafujobsportal.admin.ChatActivity;
import com.example.kafujobsportal.models.FAQsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportFragment extends Fragment {

    private RecyclerView recyclerView;
    private FAQsAdapterUser faqAdapter;
    private List<FAQsModel> faqList;
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_support, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        webView = view.findViewById(R.id.webView);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        faqList = new ArrayList<>();
        faqAdapter = new FAQsAdapterUser(getContext(), faqList);
        recyclerView.setAdapter(faqAdapter);

        fetchFAQs();
        loadLocalHtml();
        view.findViewById(R.id.chat).setOnClickListener(v -> startActivity(new Intent(view.getContext(), ChatActivity.class)));

        return view;
    }

    // Fetch FAQs from Firebase (or you could load local data)
    private void fetchFAQs() {
        FirebaseDatabase.getInstance().getReference("faqs")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        faqList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            FAQsModel faq = data.getValue(FAQsModel.class);
                            if (faq != null) {
                                faqList.add(faq);
                            }
                        }
                        // Sort FAQs by creation time (latest created first)
                        Collections.sort(faqList, (f1, f2) -> Long.compare(f2.getCreatedAt(), f1.getCreatedAt()));

                        if (faqList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        faqAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to fetch FAQs", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Load a local HTML file from the assets folder into the WebView
    private void loadLocalHtml() {
        // Place your HTML file (e.g., help.html) in the assets folder
        webView.getSettings().setJavaScriptEnabled(true);  // Enable JavaScript if needed
        webView.loadUrl("file:///android_asset/help.html");
    }
}
