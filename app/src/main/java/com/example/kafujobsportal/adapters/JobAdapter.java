package com.example.kafujobsportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.models.JobModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private final Context context;
    private final ArrayList<JobModel> jobList;

    public JobAdapter(Context context, ArrayList<JobModel> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobModel job = jobList.get(position);

        // Set job details
        holder.tvTitle.setText(job.getTitle());
        holder.tvDescription.setText(job.getDescription());
        holder.tvDueDate.setText("Due Date: " + job.getDueDate());
        holder.tvStatus.setText(job.getStatus());

        // Handle job document viewing
        holder.btnViewDocument.setOnClickListener(v -> {
            if (job.getSupportingDocumentUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(job.getSupportingDocumentUrl()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No document available", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle job deletion
        holder.btnDeleteJob.setOnClickListener(v -> deleteJob(job, holder.getAdapterPosition()));

        // Handle job status update
        holder.btnUpdateJob.setOnClickListener(v -> updateJobStatus(job));
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    /**
     * Deletes a job from Firebase and updates the RecyclerView.
     */
    private void deleteJob(JobModel job, int position) {
        DatabaseReference jobRef = Utils.getDatabaseReference(Constants.JOB_DB).child(job.getId());

        jobRef.removeValue()
                .addOnSuccessListener(unused -> {
                    jobList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Job deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete job: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the job status in Firebase and shows a Toast message.
     */
    private void updateJobStatus(JobModel job) {
        DatabaseReference jobRef = Utils.getDatabaseReference(Constants.JOB_DB).child(job.getId());

        // Status options
        String[] statuses = {"Open", "Closed", "Cancelled"};

        // Find the current status index
        int currentIndex = -1;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(job.getStatus())) {
                currentIndex = i;
                break;
            }
        }

        // Get the next status in the array (loop back to start if at the end)
        int nextIndex = (currentIndex + 1) % statuses.length;
        String newStatus = statuses[nextIndex];

        // Update Firebase
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        jobRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    job.setStatus(newStatus);
                    notifyDataSetChanged(); // Refresh UI
                    Toast.makeText(context, "Job status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update job: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    public static class JobViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvTitle, tvDescription, tvDueDate;
        Chip tvStatus;
        MaterialButton btnDeleteJob, btnUpdateJob, btnViewDocument;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnDeleteJob = itemView.findViewById(R.id.btn_delete_job);
            btnUpdateJob = itemView.findViewById(R.id.btn_update_job);
            btnViewDocument = itemView.findViewById(R.id.btn_view_document);
        }
    }
}
