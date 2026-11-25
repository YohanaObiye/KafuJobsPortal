package com.example.kafujobsportal.admin.manageJobs;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.models.JobModel;
import com.example.kafujobsportal.utils.Config;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateJobActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 101;
    private final OkHttpClient client = new OkHttpClient();
    private ImageView imgDocumentPreview;
    private MaterialButton btnSelectDocument;
    private MaterialButton createButton;
    private TextInputEditText edTitle;
    private TextInputEditText edDueDate;
    private TextInputEditText edDescription;
    private TextInputLayout tilDueDate;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickDocumentLauncher;
    private ProgressDialog progressDialog;
    private Uri selectedDocumentUri;
    private String dueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        edTitle = findViewById(R.id.ed_title);
        edDueDate = findViewById(R.id.ed_due_date);
        edDescription = findViewById(R.id.ed_description);
        tilDueDate = findViewById(R.id.til_due_date);
        btnSelectDocument = findViewById(R.id.btn_select_document);
        createButton = findViewById(R.id.create);

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (boolean isGranted : permissions.values()) {
                        allGranted = allGranted && isGranted;
                    }
                    if (allGranted) {
                        openDocumentPicker();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Initialize document picker launcher
        pickDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedDocumentUri = result.getData().getData();
                        // Optionally, show a preview or file name
                        TextView tvDocumentName = new TextView(this);
                        tvDocumentName.setText(getFileName(selectedDocumentUri));
//                        imgDocumentPreview = findViewById(R.id.img_document_preview);
//                        imgDocumentPreview.setVisibility(ImageView.GONE); // Hide image preview for documents
                    }
                }
        );

        btnSelectDocument.setOnClickListener(v -> requestPermissionsBasedOnVersion());

        edDueDate.setOnClickListener(v -> openDueDatePicker());

        createButton.setOnClickListener(v -> validateAndSendData());
    }

    private void requestPermissionsBasedOnVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            });
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // All types of files
        pickDocumentLauncher.launch(intent);
    }

    private void openDueDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER_DUE");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            dueDate = formatDate(selection);
            edDueDate.setText(dueDate);
        });
    }

    private String formatDate(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date(timeInMillis));
    }

    private void validateAndSendData() {
        String title = edTitle.getText().toString().trim();
        String description = edDescription.getText().toString().trim();

        if (title.isEmpty()) {
            edTitle.setError("Title is required");
            return;
        }


        if (dueDate == null || dueDate.isEmpty()) {
            tilDueDate.setError("Due date is required");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false); // Ensures strict date parsing

        try {
            Date dueDateObj = sdf.parse(dueDate);
            Date today = new Date(); // Get current date

            // Check if the due date is in the past or today
            if (!dueDateObj.after(today)) {
                tilDueDate.setError("Select future date");
                return;
            }
        } catch (ParseException e) {
            tilDueDate.setError("Invalid date format. Use yyyy-MM-dd");
        }


        if (description.isEmpty()) {
            edDescription.setError("Description is required");
            return;
        }

        if (selectedDocumentUri == null) {
            Toast.makeText(this, "Please select a document", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadDocument(title, description, dueDate);
    }

    private void uploadDocument(String title, String description, String dueDate) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading document...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Generate a unique file name
        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(selectedDocumentUri);

        // Convert Uri to byte array
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ContentResolver contentResolver = getContentResolver();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                contentResolver.openInputStream(selectedDocumentUri).transferTo(byteArrayOutputStream);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            MediaType mediaType = MediaType.parse(contentResolver.getType(selectedDocumentUri));
            RequestBody requestFile = RequestBody.create(mediaType, byteArray);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", uniqueFileName, requestFile);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", uniqueFileName, requestFile)
                    .build();

            String uploadUrl = Config.SUPABASE_URL + "/storage/v1/object/" + Config.STORAGE_BUCKET + "/" + uniqueFileName;

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .addHeader("Authorization", Config.AUTHORIZATION_TOKEN)
                    .addHeader("apiKey", Config.API_KEY)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateJobActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d("SUPABASE_ONE_RESPONSE", responseBody);
                        String documentUrl = Config.SUPABASE_URL + "/storage/v1/object/" + Config.STORAGE_BUCKET + "/" + uniqueFileName;
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            sendToDB(title, description, dueDate, documentUrl);
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(CreateJobActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        });
                        Log.d("SUPABASE_ONE", response.toString());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to load document", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(uri);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return ".jpg"; // Default to JPEG for images
            } else if (mimeType.startsWith("application/pdf")) {
                return ".pdf";
            } else {
                return ""; // Unknown type
            }
        }
        return "";
    }

    private void sendToDB(String title, String description, String dueDate, String documentUrl) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Generate a unique ID for the job
        String jobId = Utils.getDatabaseReference(Constants.JOB_DB).push().getKey();

        // Create a Job object
        JobModel job = new JobModel(
                jobId,
                title,
                description,
                "Open",
                dueDate,
                System.currentTimeMillis(),
                documentUrl
        );

        // Push the job to Firebase Realtime Database
        if (jobId != null) {
            Utils.getDatabaseReference(Constants.JOB_DB).child(jobId).setValue(job)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateJobActivity.this, "Data sent to DB", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateJobActivity.this, "Failed to send data to DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to generate a unique ID", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String fileName = null;
        try {
            fileName = contentResolver.query(uri, null, null, null, null).getString(contentResolver.query(uri, null, null, null, null).getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }
}