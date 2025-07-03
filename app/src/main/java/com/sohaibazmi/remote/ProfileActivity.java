package com.sohaibazmi.remote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference ref;
    TextView profileText;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("users");
        profileText = findViewById(R.id.profileText);
        logoutButton = findViewById(R.id.logoutButton);

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Account")
                    .setMessage("Do you have an account?")
                    .setPositiveButton("Login", (d, w) -> startActivity(new Intent(this, LoginActivity.class)))
                    .setNegativeButton("Register", (d, w) -> startActivity(new Intent(this, RegisterActivity.class)))
                    .setCancelable(false)
                    .show();
        } else {
            loadUserProfile(user.getUid());
        }

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void loadUserProfile(String uid) {
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String earnings = snapshot.child("earnings").getValue(String.class);

                    profileText.setText("👤 Name: " + name + "\n📧 Email: " + email + "\n📱 Phone: " + phone + "\n💰 Earnings: " + earnings);
                } else {
                    profileText.setText("No profile found.");
                }
            }

            public void onCancelled(DatabaseError error) {}
        });
    }
}
