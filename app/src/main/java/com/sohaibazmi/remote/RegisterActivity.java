package com.sohaibazmi.remote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText emailField, passwordField, nameField, phoneField;
    Button registerButton;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        nameField = findViewById(R.id.nameField);
        phoneField = findViewById(R.id.phoneField);
        registerButton = findViewById(R.id.registerButton);

        auth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String name = nameField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(new User(name, email, phone, "₹10"));
                    Toast.makeText(this, "Registration Successful. ₹10 added!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
