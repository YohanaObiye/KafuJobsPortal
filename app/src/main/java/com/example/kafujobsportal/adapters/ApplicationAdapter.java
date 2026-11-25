package com.example.kafujobsportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.admin.manageApplications.ViewApplicationActivity;
import com.example.kafujobsportal.models.ApplicationModel;
import com.example.kafujobsportal.models.JobModel;
import com.example.kafujobsportal.models.UserModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ApplicationModel> applications;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public ApplicationAdapter(Context context, ArrayList<ApplicationModel> applications) {
        this.context = context;
        this.applications = applications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationModel application = applications.get(position);

        Log.d("TestingErrors", application.toString());

        // Fetch user details and update UI
        getUserData(holder.txtUserId, application.getUserId());

        // Fetch job details and update UI
        getJobDetails(holder.txtJobId, application.getJobId());

//        holder.txtDate.setText("Applied on: " + dateFormat.format(application.getCreatedAt()));
        holder.txtDate.setText("Applied on: " + application.getCreatedAt());
        if (application.getStatus() != null)
            holder.statusChip.setText(application.getStatus());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewApplicationActivity.class);
            Toast.makeText(context, "APPLICATION_ID: " + application.getApplicationId(), Toast.LENGTH_SHORT).show();
            intent.putExtra("APPLICATION_ID", application.getApplicationId());
            context.startActivity(intent);
        });
    }

    /**
     * Fetches the user details from Firebase and sets the username in the given TextView.
     */
    private void getUserData(TextView textView, String userId) {
        Utils.getDatabaseReference(Constants.USERS_DB).child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            textView.setText("Applicant: " + user.getUsername());
                        } else {
                            textView.setText("Applicant: Unknown");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textView.setText("Applicant: Error");
                    }
                });
    }

    /**
     * Fetches the job details from Firebase and sets the job title in the given TextView.
     */
    private void getJobDetails(TextView textView, String jobId) {
        Utils.getDatabaseReference(Constants.JOB_DB).child(jobId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        JobModel job = snapshot.getValue(JobModel.class);
                        if (job != null) {
                            textView.setText("Job: " + job.getTitle());
                        } else {
                            textView.setText("Job: Unknown");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textView.setText("Job: Error");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserId, txtJobId, txtDate;
        Chip statusChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserId = itemView.findViewById(R.id.txt_user_id);
            txtJobId = itemView.findViewById(R.id.txt_job_id);
            txtDate = itemView.findViewById(R.id.txt_date);
            statusChip = itemView.findViewById(R.id.chip_status);
        }
    }
}
