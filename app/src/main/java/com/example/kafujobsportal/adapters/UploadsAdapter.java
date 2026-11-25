package com.example.kafujobsportal.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.models.UploadModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class UploadsAdapter extends RecyclerView.Adapter<UploadsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<UploadModel> uploads;

    public UploadsAdapter(Context context, ArrayList<UploadModel> uploads) {
        this.context = context;
        this.uploads = uploads;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_upload, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadModel upload = uploads.get(position);
        holder.txtFileName.setText("Document: " + upload.getUrl());

        holder.btnDownload.setOnClickListener(v -> {
            Toast.makeText(context, "Loading document...", Toast.LENGTH_SHORT).show();
            Log.d("UploadURL", upload.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(upload.getUrl()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFileName;
        MaterialButton btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFileName = itemView.findViewById(R.id.txt_file_name);
            btnDownload = itemView.findViewById(R.id.btn_download);
        }
    }
}
