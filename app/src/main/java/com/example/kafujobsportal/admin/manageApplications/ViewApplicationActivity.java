package com.example.kafujobsportal.admin.manageApplications;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.adapters.UploadsAdapter;
import com.example.kafujobsportal.models.ApplicationModel;
import com.example.kafujobsportal.models.JobModel;
import com.example.kafujobsportal.models.NotificationModel;
import com.example.kafujobsportal.models.UploadModel;
import com.example.kafujobsportal.models.UserModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewApplicationActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private String applicationId;
    private TextView txtUsername, txtEmail, txtPhone, txtJobTitle, txtJobDesc, txtDueDate;
    private CircleImageView profileImage;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private ArrayList<UploadModel> uploadList;
    private UploadsAdapter uploadsAdapter;
    private String applicantPhoneNumber; // to store the applicant's phone
    private String applicantUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_application);

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        txtUsername = findViewById(R.id.txt_username);
        txtEmail = findViewById(R.id.txt_email);
        txtPhone = findViewById(R.id.txt_phone);
        txtJobTitle = findViewById(R.id.txt_job_title);
        txtJobDesc = findViewById(R.id.txt_job_description);
        txtDueDate = findViewById(R.id.txt_due_date);
        recyclerView = findViewById(R.id.recycler_view);

        // Setup RecyclerView
        uploadList = new ArrayList<>();
        uploadsAdapter = new UploadsAdapter(this, uploadList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(uploadsAdapter);

        // Get application ID
        applicationId = getIntent().getStringExtra("APPLICATION_ID");
        Toast.makeText(this, "APPLICATION_ID: " + applicationId, Toast.LENGTH_SHORT).show();

        if (applicationId == null) {
            Toast.makeText(this, "Invalid application ID!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading application...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Fetch application details
        fetchApplicationDetails();

        // Check and request SMS permission if necessary
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }

        // Approve button functionality
        FloatingActionButton approveBtn = findViewById(R.id.approve);
        approveBtn.setOnClickListener(v -> {
            // Show a confirmation dialog
            new AlertDialog.Builder(ViewApplicationActivity.this)
                    .setTitle("Confirm Approval")
                    .setMessage("Are you sure you want to approve the applicant for interview?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Update the application status to APPROVED_FOR_INTERVIEW
                        updateApplicationStatus("APPROVED_FOR_INTERVIEW", "");
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Disapprove button functionality
        FloatingActionButton disapproveBtn = findViewById(R.id.disapprove);
        disapproveBtn.setOnClickListener(v -> {
            // Create an input field for the reason
            final EditText reasonInput = new EditText(ViewApplicationActivity.this);
            new AlertDialog.Builder(ViewApplicationActivity.this)
                    .setTitle("Disapprove Application")
                    .setMessage("Enter reason for disapproval:")
                    .setView(reasonInput)
                    .setPositiveButton("Submit", (dialog, which) -> {
                        String reason = reasonInput.getText().toString().trim();
                        if (reason.isEmpty()) {
                            Toast.makeText(ViewApplicationActivity.this, "Reason is required", Toast.LENGTH_SHORT).show();
                        } else {
                            // Update the application status to DISAPPROVED with reason
                            updateApplicationStatus("DISAPPROVED", reason);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    /**
     * Updates the application status (and reason, if provided) in the database,
     * then sends an SMS to the applicant.
     */
    private void updateApplicationStatus(String newStatus, String reason) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating application...");
        pd.setCancelable(false);
        pd.show();

        // Create a map to store updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        // Only add reason if it's not empty
        if (!reason.isEmpty()) {
            updates.put("reason", reason);
        }

        // Update the application status and reason in a single call
        Utils.getDatabaseReference(Constants.APPLICATIONS_DB)
                .child(applicationId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(ViewApplicationActivity.this, "Application updated successfully", Toast.LENGTH_SHORT).show();

                    // Prepare SMS message
                    String smsMessage = "Your application status has been updated to " + newStatus;
                    if (!reason.isEmpty()) {
                        smsMessage += ". Reason: " + reason;
                    }

                    // Send SMS only if phone number is available
                    if (applicantPhoneNumber != null && !applicantPhoneNumber.isEmpty()) {
                        sendSms(applicantPhoneNumber, smsMessage);
                    } else {
                        Toast.makeText(ViewApplicationActivity.this, "Applicant phone number is not available", Toast.LENGTH_SHORT).show();
                    }

                    // Send notification
                    sendNotification(newStatus, reason);
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(ViewApplicationActivity.this, "Failed to update application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sends a notification record to the database.
     */
    private void sendNotification(String newStatus, String reason) {
        String notificationId = Utils.getDatabaseReference(Constants.NOTIFICATIONS_DB).push().getKey();
        String senderId = Utils.getCurrentUserId();  // Get currently logged-in user
        long timestamp = System.currentTimeMillis();

        if (notificationId != null && senderId != null) {
            // Create notification message
            String message = "Your application status was updated to " + newStatus;
            if (!reason.isEmpty()) {
                message += ". Reason: " + reason;
            }

            // Create notification object
            NotificationModel notification = new NotificationModel(notificationId, senderId, applicantUserId, message, timestamp);

            // Store notification in the database
            Utils.getDatabaseReference(Constants.NOTIFICATIONS_DB)
                    .child(notificationId)
                    .setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ViewApplicationActivity.this, "Notification sent", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ViewApplicationActivity.this, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Sends an SMS to the provided phone number.
     * Note: Ensure that SEND_SMS permission is granted.
     */
    private void sendSms(String phone, String message) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches application details including user and job info.
     */
    private void fetchApplicationDetails() {
        Utils.getDatabaseReference(Constants.APPLICATIONS_DB)
                .child(applicationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ApplicationModel application = snapshot.getValue(ApplicationModel.class);
                        if (application != null) {
                            fetchUserDetails(application.getUserId());
                            fetchJobDetails(application.getJobId());
                            fetchUploadedDocuments(application.getApplicationId());
                        } else {
                            Toast.makeText(ViewApplicationActivity.this, "Application not found!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewApplicationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    /**
     * Fetches user details (name, email, phone).
     */
    private void fetchUserDetails(String userId) {
        Utils.getDatabaseReference(Constants.USERS_DB)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            applicantUserId = user.getId();
                            txtUsername.setText(user.getUsername());
                            txtEmail.setText(user.getEmail());
                            applicantPhoneNumber = user.getPhone(); // store applicant's phone
                            if (user.getPhone() == null)
                                applicantPhoneNumber = "254797147842";
                            txtPhone.setText("Phone: " + applicantPhoneNumber);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        txtUsername.setText("Unknown User");
                        progressDialog.dismiss();
                    }
                });
    }

    /**
     * Fetches job details (title, description, due date).
     */
    private void fetchJobDetails(String jobId) {
        Utils.getDatabaseReference(Constants.JOB_DB)
                .child(jobId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        JobModel job = snapshot.getValue(JobModel.class);
                        if (job != null) {
                            txtJobTitle.setText(job.getTitle());
                            txtJobDesc.setText(job.getDescription());
                            txtDueDate.setText("Due Date: " + job.getDueDate());
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        txtJobTitle.setText("Unknown Job");
                        progressDialog.dismiss();
                    }
                });
    }

    /**
     * Fetches the applicant’s uploaded documents.
     */
    private void fetchUploadedDocuments(String applicationId) {
        Utils.getDatabaseReference(Constants.APPLICATIONS_UPLOADS_DB)
                .orderByChild("applicationId")
                .equalTo(applicationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        uploadList.clear();
                        for (DataSnapshot documentSnapshot : snapshot.getChildren()) {
                            UploadModel upload = documentSnapshot.getValue(UploadModel.class);
                            if (upload != null) {
                                uploadList.add(upload);
                            }
                        }
                        uploadsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewApplicationActivity.this, "Error loading documents", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Handle the result of the SMS permission request if needed.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "SMS permission denied. SMS notifications will not work.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
