package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.binbuddy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Question extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView questionTextView;
    private Button optionA, optionB, optionC, optionD;
    private TextView onethree;
    private String level, username;
    private int currentQuestion = 1;
    private final int totalQuestions = 3;
    private int attemptCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(this);
        username = sessionManager.getLoggedInUser();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.question);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        questionTextView = findViewById(R.id.textView);
        optionA = findViewById(R.id.button1);
        optionB = findViewById(R.id.button2);
        optionC = findViewById(R.id.button3);
        optionD = findViewById(R.id.button4);
        onethree = findViewById(R.id.onethree);

        // Reset attempt count and current question when the activity is created
        currentQuestion = 1;
        attemptCount = 0;

        // Retrieve the level passed from MainActivity
        level = getIntent().getStringExtra("level");

        // Load the first question
        if (level != null) {
            loadQuizQuestion(level);
        }

        // Set the initial "1/3" for the first question
        onethree.setText(currentQuestion + "/" + totalQuestions);

        // Set up back button functionality
        ImageView back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> {
            Intent intent = new Intent(Question.this, QuizPage.class);
            startActivity(intent);
        });
    }

    // List to track shown question IDs
    private List<String> shownQuestionIds = new ArrayList<>();

    private void loadQuizQuestion(String level) {
        db.collection("quiz")
                .whereEqualTo("level", level)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                        // Filter out documents already shown
                        List<DocumentSnapshot> unseenDocuments = new ArrayList<>();
                        for (DocumentSnapshot doc : documents) {
                            if (!shownQuestionIds.contains(doc.getId())) {
                                unseenDocuments.add(doc);
                            }
                        }

                        // Check if there are any unseen questions
                        if (!unseenDocuments.isEmpty()) {
                            // Show the first unseen question
                            DocumentSnapshot documentSnapshot = unseenDocuments.get(0);

                            // Add to shown questions list
                            shownQuestionIds.add(documentSnapshot.getId());

                            // Parse the data into UI elements
                            String question = documentSnapshot.getString("question");
                            String optA = "A. " + documentSnapshot.getString("optionA");
                            String optB = "B. " + documentSnapshot.getString("optionB");
                            String optC = "C. " + documentSnapshot.getString("optionC");
                            String optD = "D. " + documentSnapshot.getString("optionD");
                            String correctOption = documentSnapshot.getString("correctOption");

                            // Update UI with the fetched question and options
                            questionTextView.setText(question);
                            optionA.setText(optA);
                            optionB.setText(optB);
                            optionC.setText(optC);
                            optionD.setText(optD);

                            // Set listeners for the buttons to check the correct answer
                            optionA.setOnClickListener(v -> checkAnswer("A", correctOption));
                            optionB.setOnClickListener(v -> checkAnswer("B", correctOption));
                            optionC.setOnClickListener(v -> checkAnswer("C", correctOption));
                            optionD.setOnClickListener(v -> checkAnswer("D", correctOption));
                        } else {
                            Toast.makeText(this, "No more unseen questions available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading question", Toast.LENGTH_SHORT).show();
                });
    }


    private void checkAnswer(String selectedOption, String correctOption) {
        Log.d("Quiz", "Selected Option: " + selectedOption + ", Correct Option: " + correctOption);

        boolean isCorrect = selectedOption.equals(correctOption);
        final int[] score = {0};

        if (isCorrect) {
            Log.d("Quiz", "Correct answer selected");

            // Retrieve the score for the level
            db.collection("score")
                    .whereEqualTo("level", level)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            score[0] = documentSnapshot.getLong("value").intValue();
                            Log.d("Quiz", "Score retrieved: " + score[0]);

                            // Create a QuizResult object to store the result
                            QuizResult result = new QuizResult(
                                    username,  // Replace with the actual username (pass it from MainActivity)
                                    level,
                                    questionTextView.getText().toString(),
                                    selectedOption,
                                    isCorrect,
                                    score[0]
                            );

                            // Save the result in quizResult collection
                            db.collection("quizResult")
                                    .add(result)
                                    .addOnSuccessListener(documentReference -> {
                                        // Update the points if the answer is correct
                                        updatePoints(username, score[0]); // Add points to user's pointEarned field
                                        showSuccessDialog();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error saving result", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching score", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.d("Quiz", "Incorrect answer selected");

            // Create a QuizResult object to store the result
            QuizResult result = new QuizResult(
                    username,  // Replace with the actual username (pass it from MainActivity)
                    level,
                    questionTextView.getText().toString(),
                    selectedOption,
                    isCorrect,
                    score[0]
            );

            // Save the result in quizResult collection
            db.collection("quizResult")
                    .add(result)
                    .addOnSuccessListener(documentReference -> {
                        showFailDialog();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving result", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updatePoints(String username, int score) {
        // Fetch the user's current points from the "point" collection
        db.collection("point")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User's point record exists
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        int currentPoints = documentSnapshot.getLong("pointEarned").intValue();

                        // Update the pointEarned field
                        int updatedPoints = currentPoints + score;

                        // Save the updated points back to Firestore
                        db.collection("point")
                                .document(documentSnapshot.getId())
                                .update("pointEarned", updatedPoints)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Quiz", "Points updated successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Quiz", "Error updating points: ", e);
                                    Toast.makeText(this, "Error updating points", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // If no document found for the user, create a new point record
                        Map<String, Object> pointData = new HashMap<>();
                        pointData.put("username", username);
                        pointData.put("pointEarned", score);  // Start the user's points with the score

                        db.collection("point")
                                .add(pointData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("Quiz", "New point record created for user: " + username);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Quiz", "Error creating new point record: ", e);
                                    Toast.makeText(this, "Error creating new point record", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Quiz", "Error fetching user's points: ", e);
                    Toast.makeText(this, "Error fetching user's points", Toast.LENGTH_SHORT).show();
                });
    }


    private void showSuccessDialog() {
        Log.d("Dialog", "Showing Success Dialog");
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView congratulationsText = dialog.findViewById(R.id.congratulations_text);
        TextView descriptionText = dialog.findViewById(R.id.description_text);
        Button nextButton = dialog.findViewById(R.id.next_button);

        congratulationsText.setText("Congratulations!");
        descriptionText.setText("Well done!\nFantastic job!");
        nextButton.setText(currentQuestion == totalQuestions ? "Close" : "Next");

        nextButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (currentQuestion < totalQuestions) {
                loadQuizQuestion(level);
                currentQuestion++;
                onethree.setText(currentQuestion + "/" + totalQuestions);
            } else {
                Intent intent = new Intent(Question.this, QuizPage.class);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    private void showFailDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_fail);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView congratulationsText = dialog.findViewById(R.id.congratulations_text);
        TextView descriptionText = dialog.findViewById(R.id.description_text);
        Button nextButton = dialog.findViewById(R.id.next_button);

        congratulationsText.setText("Oops! Incorrect Answer");
        descriptionText.setText("Nice try! Keep it up!");
        nextButton.setText(currentQuestion == totalQuestions ? "Close" : "Next");

        nextButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (currentQuestion < totalQuestions) {
                loadQuizQuestion(level);
                currentQuestion++;
                onethree.setText(currentQuestion + "/" + totalQuestions);
            } else {
                Intent intent = new Intent(Question.this, QuizPage.class);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }
}
