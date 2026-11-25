package com.example.kafujobsportal.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.DocumentAdapter;
import com.example.kafujobsportal.models.ApplicationModel;
import com.example.kafujobsportal.models.DocumentModel;
import com.example.kafujobsportal.models.JobModel;
import com.example.kafujobsportal.models.UploadModel;
import com.example.kafujobsportal.utils.Config;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApplyActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private final List<DocumentModel> documentList = new ArrayList<>();
    private TextView txtJobName, txtJobDescription;
    private Chip chipDueDate, chipStatus;
    private DatabaseReference jobsRef;
    private String jobId;
    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private Button btnSelectDocument;
    private Uri selectedFileUri;
    private String documentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize UI Components
        txtJobName = findViewById(R.id.txt_job_name);
        txtJobDescription = findViewById(R.id.txt_job_description);
        chipDueDate = findViewById(R.id.chip_deu_date);
        chipStatus = findViewById(R.id.chip_status);

        // Initialize Firebase Database
        jobsRef = FirebaseDatabase.getInstance().getReference("jobs");

        // Get Job ID from Intent
        jobId = getIntent().getStringExtra("JOB_ID");
        if (jobId == null) {
            Toast.makeText(this, "Job not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recycler_view);
        btnSelectDocument = findViewById(R.id.btn_select_document);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(documentList, this);
        recyclerView.setAdapter(adapter);

        btnSelectDocument.setOnClickListener(v -> showFileNameDialog());

        findViewById(R.id.btn_apply).setOnClickListener(v -> {
            applyForJob();
        });

    }

    private void applyForJob() {
        String userId = Utils.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting application...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String applicationId = UUID.randomUUID().toString();
        long createdAt = System.currentTimeMillis();

        // Create application object
        DatabaseReference applicationsRef = FirebaseDatabase.getInstance().getReference(Constants.APPLICATIONS_DB);
        applicationsRef.child(applicationId).setValue(new ApplicationModel(applicationId, userId, jobId, "PENDING", "", createdAt))
                .addOnSuccessListener(aVoid -> {
                    saveUploadedDocuments(applicationId, progressDialog);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to apply: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUploadedDocuments(String applicationId, ProgressDialog progressDialog) {
        DatabaseReference uploadsRef = FirebaseDatabase.getInstance().getReference(Constants.APPLICATIONS_UPLOADS_DB);

        if (documentList.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        for (DocumentModel document : documentList) {
            String docId = UUID.randomUUID().toString();
            UploadModel upload = new UploadModel(applicationId, docId, document.getUrl(), System.currentTimeMillis());

            uploadsRef.child(docId).setValue(upload).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to save document uploads!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchJobDetails();
    }

    private void showFileNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Document Name");
        builder.setIcon(R.drawable.logo);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            documentName = input.getText().toString().trim();
            if (documentName.isEmpty())
                Toast.makeText(this, "The document name is required", Toast.LENGTH_SHORT).show();
            else openFilePicker();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                uploadFileToSupabase(selectedFileUri, documentName);
            }
        }
    }

    private void uploadFileToSupabase(Uri fileUri, String docName) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(fileUri);

        try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            if (inputStream == null) throw new IOException("Failed to open input stream");

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] fileBytes = byteArrayOutputStream.toByteArray();
            MediaType mediaType = MediaType.parse(getContentResolver().getType(fileUri));
            RequestBody requestBody = RequestBody.create(mediaType, fileBytes);

            String uploadUrl = Config.SUPABASE_URL + "/storage/v1/object/" + Config.STORAGE_BUCKET + "/applications/" +
                    "." +
                    ".." + uniqueFileName;

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .addHeader("Authorization", Config.AUTHORIZATION_TOKEN)
                    .addHeader("apiKey", Config.API_KEY)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (response.isSuccessful()) {
                            String fileUrl = Config.SUPABASE_URL + "/storage/v1/object/public/" + Config.STORAGE_BUCKET + "/" + uniqueFileName;
                            documentList.add(new DocumentModel(docName, fileUrl));
                            adapter.notifyDataSetChanged();
                            Toast.makeText(ApplyActivity.this, "Upload successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ApplyActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(ApplyActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        String type = getContentResolver().getType(uri);
        return type != null ? "." + type.substring(type.lastIndexOf("/") + 1) : "";
    }

    private void fetchJobDetails() {
        jobsRef.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JobModel job = snapshot.getValue(JobModel.class);
                if (job != null) {
                    txtJobName.setText(job.getTitle());
                    txtJobDescription.setText(job.getDescription());
                    chipDueDate.setText("Due Date: " + job.getDueDate());
                    chipStatus.setText(job.getStatus());
                } else {
                    Toast.makeText(ApplyActivity.this, "Job details not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ApplyActivity.this, "Failed to load job details!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
