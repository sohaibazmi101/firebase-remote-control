package com.sohaibazmi.remote;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QRResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_result);

        TextView txtResult = findViewById(R.id.txt_result);

        String qrResult = getIntent().getStringExtra("qr_result");
        if (qrResult != null) {
            txtResult.setText("Scanned QR:\n\n" + qrResult);
        } else {
            txtResult.setText("No QR code scanned.");
        }
    }
}
