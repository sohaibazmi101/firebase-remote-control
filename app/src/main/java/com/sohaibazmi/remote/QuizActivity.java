package com.sohaibazmi.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    TextView questionText, timerText, resultText;
    Button optionA, optionB, optionC, optionD, nextButton;

    RequestQueue queue;
    CountDownTimer timer;
    int correctIndex = -1;
    boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        resultText = findViewById(R.id.resultText);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextButton = findViewById(R.id.nextButton);

        queue = Volley.newRequestQueue(this);

        loadNewQuestion();

        nextButton.setOnClickListener(v -> {
            enableButtons(true);
            nextButton.setVisibility(View.GONE);
            resultText.setText("");
            loadNewQuestion();
        });
    }

    private void loadNewQuestion() {
        answered = false;

        if (!isInternetAvailable()) {
            Toast.makeText(this, "❌ No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://opentdb.com/api.php?amount=1&type=multiple";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        JSONObject questionObj = results.getJSONObject(0);

                        String question = Html.fromHtml(questionObj.getString("question")).toString();
                        String correctAnswer = Html.fromHtml(questionObj.getString("correct_answer")).toString();

                        JSONArray incorrectAnswers = questionObj.getJSONArray("incorrect_answers");
                        List<String> allOptions = new ArrayList<>();
                        for (int i = 0; i < incorrectAnswers.length(); i++) {
                            allOptions.add(Html.fromHtml(incorrectAnswers.getString(i)).toString());
                        }
                        allOptions.add(correctAnswer);
                        Collections.shuffle(allOptions);

                        questionText.setText(question);
                        optionA.setText(allOptions.get(0));
                        optionB.setText(allOptions.get(1));
                        optionC.setText(allOptions.get(2));
                        optionD.setText(allOptions.get(3));

                        correctIndex = allOptions.indexOf(correctAnswer);
                        setButtonListeners(allOptions);
                        startTimer();

                    } catch (JSONException e) {
                        Toast.makeText(this, "❌ Error loading question.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "❌ Network error, please try again.", Toast.LENGTH_SHORT).show()
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(jsonObjectRequest);
    }

    private void setButtonListeners(List<String> options) {
        View.OnClickListener listener = v -> {
            if (answered) return;
            if (timer != null) timer.cancel();

            Button selectedButton = (Button) v;
            int selectedIndex = options.indexOf(selectedButton.getText().toString());

            answered = true;
            enableButtons(false);

            if (selectedIndex == correctIndex) {
                resultText.setText("✅ Correct! +₹1");
                updateEarnings(1.0);
            } else {
                resultText.setText("❌ Incorrect! -₹1");
                updateEarnings(-1.0);
            }

            nextButton.setVisibility(View.VISIBLE);
        };

        optionA.setOnClickListener(listener);
        optionB.setOnClickListener(listener);
        optionC.setOnClickListener(listener);
        optionD.setOnClickListener(listener);
    }

    private void startTimer() {
        if (timer != null) timer.cancel();

        timer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("⏰ Time: " + (millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                if (!answered) {
                    resultText.setText("⚠ Not Attempted! -₹0.50");
                    updateEarnings(-0.5);
                    enableButtons(false);
                    nextButton.setVisibility(View.VISIBLE);
                    answered = true;
                }
            }
        }.start();
    }

    private void updateEarnings(double change) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("earnings");

        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String oldStr = snapshot.getValue(String.class).replace("₹", "").trim();
                double current = Double.parseDouble(oldStr);
                double newAmount = current + change;
                if (newAmount < 0) newAmount = 0;
                ref.setValue("₹" + newAmount);
            }
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void enableButtons(boolean enable) {
        optionA.setEnabled(enable);
        optionB.setEnabled(enable);
        optionC.setEnabled(enable);
        optionD.setEnabled(enable);
    }
}
