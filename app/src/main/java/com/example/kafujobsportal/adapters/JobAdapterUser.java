package com.example.kafujobsportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.models.JobModel;
import com.example.kafujobsportal.user.ApplyActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class JobAdapterUser extends RecyclerView.Adapter<JobAdapterUser.JobViewHolder> {

    private final Context context;
    private final List<JobModel> jobList;

    public JobAdapterUser(Context context, List<JobModel> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job_user, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobModel job = jobList.get(position);

        holder.tvTitle.setText(job.getTitle());
        holder.tvDueDate.setText("Due Date: " + job.getDueDate());
        holder.tvDescription.setText(job.getDescription());
        holder.tvStatus.setText(job.getStatus());

        // Open ApplyActivity and pass the Job ID
        holder.btnApply.setOnClickListener(v -> {
            Intent intent = new Intent(context, ApplyActivity.class);
            intent.putExtra("JOB_ID", job.getId()); // Pass Job ID
            context.startActivity(intent);
        });

        // Handle PDF Download
        holder.btnViewDocument.setOnClickListener(v -> openPdf(job.getSupportingDocumentUrl()));
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    private void openPdf(String pdfUrl) {
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(context, "No document available!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(pdfUrl));
        context.startActivity(intent);
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDueDate, tvDescription;
        Chip tvStatus;
        MaterialButton btnApply, btnViewDocument;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnApply = itemView.findViewById(R.id.btn_apply);
            btnViewDocument = itemView.findViewById(R.id.btn_view_document);
        }
    }
}
