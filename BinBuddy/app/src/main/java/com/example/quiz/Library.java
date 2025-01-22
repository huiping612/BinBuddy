package com.example.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.binbuddy.R;

public class Library extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView totalquiz, number1, number2, number3, c1, c2, c3, c4, quizcategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        UserSessionManager sessionManager = new UserSessionManager(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.library);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Reference the TextViews
        totalquiz = findViewById(R.id.totalquiz);
        number1 = findViewById(R.id.number1);
        number2 = findViewById(R.id.number2);
        number3 = findViewById(R.id.number3);
        c1 = findViewById(R.id.c1);
        c2 = findViewById(R.id.c2);
        c3 = findViewById(R.id.c3);
        c4 = findViewById(R.id.c4);
        quizcategory = findViewById(R.id.quizcategory);

        // Set up the Quiz Category title
        quizcategory.setText("Quiz Category");

        // Set up the back button functionality
        ImageView back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                finish();
            }
        });

        // Get the username from shared preferences or wherever you store it
        String username = sessionManager.getLoggedInUser();  // Replace with actual logic to retrieve the username

        // Fetch the data from Firestore
        getUserStats(username);

        // Set click listeners for c1, c2, and c3
        c1.setOnClickListener(v -> openQuestionList("beginner"));
        c2.setOnClickListener(v -> openQuestionList("intermediate"));
        c3.setOnClickListener(v -> openQuestionList("advanced"));

        c4 = findViewById(R.id.c4);

        // When the c4 button is pressed, navigate to ReviewMistakes (question.xml)
        c4.setOnClickListener(v -> {
            Intent intent = new Intent(Library.this, ReviewMistakes.class);
            startActivity(intent);
        });


    }

    // Method to open QuestionListActivity
    private void openQuestionList(String level) {
        Intent intent = new Intent(Library.this, QuestionListActivity.class);
        intent.putExtra("level", level);
        startActivity(intent);
    }


    private void getUserStats(String username) {
        db.collection("quizResult")
                .whereEqualTo("username", username)  // Query by username
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d("Library", "Found quiz results for the user.");

                        // Initialize counters for total attempts, correct, and incorrect answers by level
                        int totalAttempted = 0;
                        int correctBeginner = 0, correctIntermediate = 0, correctAdvanced = 0;
                        int incorrectBeginner = 0, incorrectIntermediate = 0, incorrectAdvanced = 0;

                        // Loop through the results to calculate the correct and incorrect answers per level
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            totalAttempted++;  // Increment the total quizzes attempted
                            String level = doc.getString("level");  // Get level (beginner, intermediate, advanced)
                            Boolean isCorrect = doc.getBoolean("correct");  // Get whether the answer was correct

                            Log.d("Library", "Level: " + level + ", Correct: " + isCorrect);

                            // Count correct and incorrect answers by level
                            if (isCorrect != null) {
                                if (isCorrect) {
                                    if ("beginner".equals(level)) {
                                        correctBeginner++;
                                    } else if ("intermediate".equals(level)) {
                                        correctIntermediate++;
                                    } else if ("advanced".equals(level)) {
                                        correctAdvanced++;
                                    }
                                } else {
                                    if ("beginner".equals(level)) {
                                        incorrectBeginner++;
                                    } else if ("intermediate".equals(level)) {
                                        incorrectIntermediate++;
                                    } else if ("advanced".equals(level)) {
                                        incorrectAdvanced++;
                                    }
                                }
                            }
                        }

                        // Log the calculated values
                        Log.d("Library", "Correct Beginner: " + correctBeginner);
                        Log.d("Library", "Correct Intermediate: " + correctIntermediate);
                        Log.d("Library", "Correct Advanced: " + correctAdvanced);
                        Log.d("Library", "Incorrect Beginner: " + incorrectBeginner);
                        Log.d("Library", "Incorrect Intermediate: " + incorrectIntermediate);
                        Log.d("Library", "Incorrect Advanced: " + incorrectAdvanced);

                        // Update the UI with the fetched data
                        totalquiz.setText("Total quizzes attempted: " + totalAttempted);

                        // number1, number2, number3 now show both correct and incorrect counts
                        number1.setText(String.valueOf(correctBeginner + incorrectBeginner));  // Total (correct + incorrect) for Beginner
                        number2.setText(String.valueOf(correctIntermediate + incorrectIntermediate));  // Total for Intermediate
                        number3.setText(String.valueOf(correctAdvanced + incorrectAdvanced));  // Total for Advanced

                        // Set Achievements for each level (only correct answers)
                        c1.setText("Achievements: " + correctBeginner);
                        c2.setText("Achievements: " + correctIntermediate);
                        c3.setText("Achievements: " + correctAdvanced);

                        // Calculate Incorrect Answers
                        int incorrectAnswers = (incorrectBeginner + incorrectIntermediate + incorrectAdvanced);
                        c4.setText("Incorrect Answers: " + incorrectAnswers);

                    } else {
                        Log.d("Library", "No data found for this user.");
                        // If no results found, set a default message or handle as needed
                        totalquiz.setText("Total quizzes attempted: 0");
                        number1.setText("0");
                        number2.setText("0");
                        number3.setText("0");
                        c1.setText("Achievements: 0");
                        c2.setText("Achievements: 0");
                        c3.setText("Achievements: 0");
                        c4.setText("Incorrect Answers: 0");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Library", "Error fetching user stats", e);
                    // Show a failure message if needed
                    totalquiz.setText("Error loading stats.");
                });
    }
}
