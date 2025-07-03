package com.sohaibazmi.remote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ContactDeveloperActivity extends AppCompatActivity {

    EditText nameInput, numberInput, messageInput;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_developer);

        nameInput = findViewById(R.id.nameInput);
        numberInput = findViewById(R.id.numberInput);
        messageInput = findViewById(R.id.messageInput);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> sendMessageToTelegram());
    }

    private void sendMessageToTelegram() {
        String name = nameInput.getText().toString().trim();
        String number = numberInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        if (name.isEmpty() || number.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullMessage = "📩 New Message from App\n\n👤 Name: " + name + "\n📱 Number: " + number + "\n📝 Message: " + message;

        // Call the static method in RemoteService
        RemoteService.sendTelegramText(fullMessage);

        Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
