package com.example.kafujobsportal.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Utils {

    /**
     * Returns a DatabaseReference for the specified child/table.
     *
     * @param child The child or table name.
     * @return A DatabaseReference for the specified child/table.
     */
    public static DatabaseReference getDatabaseReference(String child) {
        return FirebaseDatabase.getInstance().getReference(child);
    }

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }
}