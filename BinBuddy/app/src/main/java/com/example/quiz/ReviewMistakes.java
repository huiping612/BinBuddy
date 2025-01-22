package com.example.quiz;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.binbuddy.R;

import java.util.HashMap;

public class ReviewMistakes extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView questionText, scoreText;
    private Button button1, button2, button3, button4;
    private String currentQuestionText, username;
    private int currentQuestionIndex = 0;
    private QuerySnapshot incorrectQuestionsSnapshot;
    private int totalQuestions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question);

        UserSessionManager sessionManager = new UserSessionManager(this);
        username = sessionManager.getLoggedInUser();


        db = FirebaseFirestore.getInstance();

        questionText = findViewById(R.id.textView);
        scoreText = findViewById(R.id.onethree);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        fetchIncorrectAnswers(username);

        // Set up the back button functionality
        ImageView back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Library activity
                Intent intent = new Intent(ReviewMistakes.this, Library.class);
                startActivity(intent);  // Start the Library activity
                finish();  // Optionally finish the current activity if you want to remove it from the back stack
            }
        });

    }

    private void fetchIncorrectAnswers(String username) {
        db.collection("quizResult")
                .whereEqualTo("username", username)
                .whereEqualTo("correct", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        incorrectQuestionsSnapshot = queryDocumentSnapshots;
                        totalQuestions = queryDocumentSnapshots.size();
                        showNextIncorrectQuestion();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void showNextIncorrectQuestion() {
        if (currentQuestionIndex < incorrectQuestionsSnapshot.size()) {
            QueryDocumentSnapshot document = (QueryDocumentSnapshot) incorrectQuestionsSnapshot.getDocuments().get(currentQuestionIndex);
            String question = document.getString("question");

            currentQuestionText = question;
            fetchQuestionDetails(question);
        } else {
            showSuccessDialog(); // Show success dialog if all questions are answered correctly
        }
    }

    private void fetchQuestionDetails(String questionTextInput) {
        db.collection("quiz")
                .whereEqualTo("question", questionTextInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        String optionA = documentSnapshot.getString("optionA");
                        String optionB = documentSnapshot.getString("optionB");
                        String optionC = documentSnapshot.getString("optionC");
                        String optionD = documentSnapshot.getString("optionD");

                        questionText.setText(currentQuestionText);
                        button1.setText("A. " + optionA);
                        button2.setText("B. " + optionB);
                        button3.setText("C. " + optionC);
                        button4.setText("D. " + optionD);

                        String correctAnswer = documentSnapshot.getString("correctOption");
                        scoreText.setText((currentQuestionIndex + 1) + "/" + totalQuestions);

                        button1.setOnClickListener(v -> checkAnswer("A", correctAnswer));
                        button2.setOnClickListener(v -> checkAnswer("B", correctAnswer));
                        button3.setOnClickListener(v -> checkAnswer("C", correctAnswer));
                        button4.setOnClickListener(v -> checkAnswer("D", correctAnswer));
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error fetching question details
                });
    }

    private void checkAnswer(String selectedOption, String correctAnswer) {
        boolean isCorrect = selectedOption.equals(correctAnswer);

        if (isCorrect) {
            updateQuizResult(true);
            showSuccessDialog();  // Show success dialog for correct answer
        } else {
            showFailDialog();  // Show failure dialog for incorrect answer
        }
    }

    private void updateQuizResult(boolean isCorrect) {
        db.collection("quizResult")
                .whereEqualTo("question", currentQuestionText)
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        db.collection("quizResult")
                                .document(documentSnapshot.getId())
                                .update("correct", isCorrect, "score", isCorrect ? 1 : 0);

                        if (isCorrect) {
                            updateUserPoints(username);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error during update
                });
    }

    private void updateUserPoints(String username) {
        db.collection("point").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentPoints = documentSnapshot.getLong("pointEarned");
                        if (currentPoints != null) {
                            db.collection("point").document(username)
                                    .update("pointEarned", currentPoints + 1)
                                    .addOnSuccessListener(aVoid -> {
                                        // Points successfully updated
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle error updating points
                                    });
                        }
                    } else {
                        // If the document doesn't exist, initialize with 1 point
                        db.collection("point").document(username)
                                .set(new HashMap<String, Object>() {{
                                    put("pointEarned", 1);
                                }})
                                .addOnSuccessListener(aVoid -> {
                                    // Points successfully initialized
                                })
                                .addOnFailureListener(e -> {
                                    // Handle error initializing points
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error fetching user points
                });
    }


    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView congratulationsText = dialog.findViewById(R.id.congratulations_text);
        TextView descriptionText = dialog.findViewById(R.id.description_text);
        Button nextButton = dialog.findViewById(R.id.next_button);

        congratulationsText.setText("Congratulations!");
        descriptionText.setText("Well done!\nFantastic job!");

        nextButton.setText(currentQuestionIndex == totalQuestions - 1 ? "Close" : "Next");

        nextButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (currentQuestionIndex < totalQuestions - 1) {
                currentQuestionIndex++;
                showNextIncorrectQuestion();
            } else {
                navigateToLibraryPage();
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

        nextButton.setText("Retry");

        nextButton.setOnClickListener(v -> {
            dialog.dismiss();
            showNextIncorrectQuestion();  // Retry the same incorrect question
        });

        dialog.show();
    }

    private void navigateToLibraryPage() {
        Intent intent = new Intent(ReviewMistakes.this, Library.class);
        startActivity(intent);  // Start the Library activity
        finish();
    }

}
