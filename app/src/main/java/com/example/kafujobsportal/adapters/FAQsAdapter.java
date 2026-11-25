package com.example.kafujobsportal.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.models.FAQsModel;
import com.example.kafujobsportal.utils.Constants;
import com.example.kafujobsportal.utils.Utils;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class FAQsAdapter extends RecyclerView.Adapter<FAQsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<FAQsModel> faqsList;

    public FAQsAdapter(Context context, ArrayList<FAQsModel> faqsList) {
        this.context = context;
        this.faqsList = faqsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.items_faqs_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FAQsModel faq = faqsList.get(position);
        holder.tvQuestion.setText(faq.getQuestion());
        holder.tvAnswer.setText(faq.getAnswer());

        // Handle delete button click
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(faq, position));
    }

    @Override
    public int getItemCount() {
        return faqsList.size();
    }

    // Show confirmation dialog before deleting
    private void showDeleteConfirmation(FAQsModel faq, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete FAQ")
                .setMessage("Are you sure you want to delete this FAQ?")
                .setPositiveButton("Yes", (dialog, which) -> deleteFAQ(faq, position))
                .setNegativeButton("No", null)
                .show();
    }

    // Delete FAQ from Firebase and update RecyclerView
    private void deleteFAQ(FAQsModel faq, int position) {
        DatabaseReference faqsRef = Utils.getDatabaseReference(Constants.FAQ_DB).child(faq.getId());

        faqsRef.removeValue().addOnSuccessListener(unused -> {
//            faqsList.remove(position);
//            notifyItemRemoved(position);
            Toast.makeText(context, "FAQ deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(context, "Failed to delete FAQ: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
