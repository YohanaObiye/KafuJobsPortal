package com.example.kafujobsportal.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kafujobsportal.R;
import com.example.kafujobsportal.admin.helpDesk.ManageHelpDeskActivity;
import com.example.kafujobsportal.admin.manageApplications.ManageApplicationsActivity;
import com.example.kafujobsportal.admin.manageJobs.ManageJobsActivity;
import com.example.kafujobsportal.admin.users.ManageUsersActivity;
import com.example.kafujobsportal.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        // Find the MaterialToolbar and set it as the action bar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.manage_users).setOnClickListener(v -> {
            Toast.makeText(this, "Manage Users Clicked", Toast.LENGTH_SHORT).show();
// Open ManageUsersActivity in the future
            Intent intent = new Intent(AdminActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.manage_applications).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageApplicationsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.help_desk).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageHelpDeskActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.manage_job_adverts).setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageJobsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {
            // Handle the logout action
            showLogoutConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setIcon(R.drawable.logo) // Set the app icon
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logoutUser() {
        // Logout the user from Firebase
        mAuth.signOut();
        // Redirect to login activity
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Close the current activity
    }


    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Confirmation")
                .setMessage("Are you sure you want to exit the app?")
                .setIcon(R.drawable.logo) // Set the app icon
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // Close the current activity
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}