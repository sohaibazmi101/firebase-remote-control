package com.sohaibazmi.remote;

import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;
import android.media.MediaPlayer;


public class DiceActivity extends AppCompatActivity {

    EditText guessInput;
    ImageView diceImage1, diceImage2;
    Button rollButton;
    TextView resultText, earningsText;

    Random random = new Random();
    String uid;
    double currentBalance = 0;
    MediaPlayer diceSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        guessInput = findViewById(R.id.guessInput);
        diceImage1 = findViewById(R.id.diceImage1);
        diceImage2 = findViewById(R.id.diceImage2);
        rollButton = findViewById(R.id.rollButton);
        resultText = findViewById(R.id.resultText);
        earningsText = findViewById(R.id.earningsText);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCurrentEarnings();

        rollButton.setOnClickListener(v -> {
            if (currentBalance < 1) {
                Toast.makeText(this, "❌ Not enough balance. You need ₹1 to play.", Toast.LENGTH_SHORT).show();
                return;
            }
            rollDice();
        });
        diceSound = MediaPlayer.create(this, R.raw.dice_roll);
    }

    private void rollDice() {
        if (diceSound != null) diceSound.start();
        String guessStr = guessInput.getText().toString().trim();
        if (guessStr.isEmpty()) {
            Toast.makeText(this, "⚠ Enter a number between 2 and 12", Toast.LENGTH_SHORT).show();
            return;
        }

        int guessedNumber = Integer.parseInt(guessStr);
        if (guessedNumber < 2 || guessedNumber > 12) {
            Toast.makeText(this, "⚠ Invalid guess. Pick 2 to 12.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button during spin
        rollButton.setEnabled(false);
        resultText.setText("🎲 Rolling...");

        // Spin animation
        RotateAnimation rotate1 = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        RotateAnimation rotate2 = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate1.setDuration(2000);
        rotate2.setDuration(2000);

        diceImage1.startAnimation(rotate1);
        diceImage2.startAnimation(rotate2);

        // Delay result after 2 seconds (duration of spin)
        new Handler().postDelayed(() -> {
            int dice1 = random.nextInt(6) + 1;
            int dice2 = random.nextInt(6) + 1;
            int total = dice1 + dice2;

            diceImage1.setImageResource(getDiceImage(dice1));
            diceImage2.setImageResource(getDiceImage(dice2));

            if (total == guessedNumber) {
                resultText.setText("🎉 You guessed right! +₹2");
                resultText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop_in));
                updateEarnings(2.0);
            } else {
                resultText.setText("❌ You lost! Total: " + total + " ➔ -₹1");
                updateEarnings(-1.0);
            }

            // Reset for next round
            guessInput.setText("");
            rollButton.setEnabled(true);
        }, 2000);
    }

    private int getDiceImage(int diceNumber) {
        switch (diceNumber) {
            case 1: return R.drawable.dice_1;
            case 2: return R.drawable.dice_2;
            case 3: return R.drawable.dice_3;
            case 4: return R.drawable.dice_4;
            case 5: return R.drawable.dice_5;
            case 6: return R.drawable.dice_6;
            default: return R.drawable.dice_1;
        }
    }

    private void updateEarnings(double change) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("earnings");

        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String oldStr = snapshot.getValue(String.class).replace("₹", "").trim();
                double current = Double.parseDouble(oldStr);
                double newAmount = current + change;
                if (newAmount < 0) newAmount = 0;

                String newEarnings = "₹" + newAmount;
                ref.setValue(newEarnings);
                earningsText.setText("Balance: " + newEarnings);
                currentBalance = newAmount;
            }
        });
    }

    private void loadCurrentEarnings() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("earnings");

        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String value = snapshot.getValue(String.class);
                earningsText.setText("Balance: " + value);
                currentBalance = Double.parseDouble(value.replace("₹", "").trim());
            } else {
                ref.setValue("₹0");
                earningsText.setText("Balance: ₹0");
                currentBalance = 0;
            }
        });
    }
}
