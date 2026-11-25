package com.example.kafujobsportal.admin.helpDesk;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.models.FAQsModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

public class CreateFAQsActivity extends AppCompatActivity {

    private TextInputEditText edQuestion, edAnswer;
    private DatabaseReference faqsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_faqs);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firebase reference
        faqsRef = Utils.getDatabaseReference(Constants.FAQ_DB);

        // UI Elements
        edQuestion = findViewById(R.id.ed_question);
        edAnswer = findViewById(R.id.ed_answer);
        MaterialButton btnCreateFAQ = findViewById(R.id.btn_create_faq);

        // Button Click Listener
        btnCreateFAQ.setOnClickListener(v -> createFAQ());
    }

    private void createFAQ() {
        String question = edQuestion.getText().toString().trim();
        String answer = edAnswer.getText().toString().trim();

        if (TextUtils.isEmpty(question)) {
            edQuestion.setError("Question is required!");
            return;
        }

        if (TextUtils.isEmpty(answer)) {
            edAnswer.setError("Answer is required!");
            return;
        }

        // Generate unique ID
        String faqId = faqsRef.push().getKey();
        long createdAt = System.currentTimeMillis();

        // Create FAQ Object
        FAQsModel faq = new FAQsModel(faqId, question, answer, createdAt);

        // Push to Firebase
        faqsRef.child(faqId).setValue(faq)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "FAQ Created Successfully!", Toast.LENGTH_SHORT).show();
                    edQuestion.setText(""); // Clear inputs
                    edAnswer.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create FAQ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
