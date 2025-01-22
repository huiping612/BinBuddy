package com.example.quiz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.binbuddy.R;

import java.util.ArrayList;
import java.util.List;

public class QuestionListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private LibraryQuestionAdapter questionAdapter;
    private List<LibraryQuestion> questionList;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        UserSessionManager sessionManager = new UserSessionManager(this);
        username = sessionManager.getLoggedInUser();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize question list and adapter
        questionList = new ArrayList<>();
        questionAdapter = new LibraryQuestionAdapter(questionList);
        recyclerView.setAdapter(questionAdapter);

        // Set up Back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish()); // Go back to the Library page

        // Get level from intent
        String level = getIntent().getStringExtra("level");
        if (level != null) {
            fetchQuestions(level);
        }
    }

    private void fetchQuestions(String level) {
        db.collection("quizResult")
                .whereEqualTo("level", level)
                .whereEqualTo("username", username)// Ensure the level is correct
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot resultDoc : queryDocumentSnapshots) {
                            String questionText = resultDoc.getString("question");  // Get the question text
                            Boolean isCorrect = resultDoc.getBoolean("correct");  // Get the correctness (true/false)

                            // Only fetch questions where the answer was correct
                            if (isCorrect != null && isCorrect) {
                                // Fetch the quiz details using the question text
                                db.collection("quiz")
                                        .whereEqualTo("question", questionText)
                                        .get()
                                        .addOnSuccessListener(quizQuery -> {
                                            if (!quizQuery.isEmpty()) {
                                                for (QueryDocumentSnapshot quizDoc : quizQuery) {
                                                    String question = quizDoc.getString("question");
                                                    String answer = "";  // Determine the answer text based on the selected option
                                                    String selectedOption = resultDoc.getString("selectedOption");

                                                    switch (selectedOption) {
                                                        case "A":
                                                            answer = quizDoc.getString("optionA");
                                                            break;
                                                        case "B":
                                                            answer = quizDoc.getString("optionB");
                                                            break;
                                                        case "C":
                                                            answer = quizDoc.getString("optionC");
                                                            break;
                                                        case "D":
                                                            answer = quizDoc.getString("optionD");
                                                            break;
                                                    }

                                                    // Add the question and correct answer to the list
                                                    questionList.add(new LibraryQuestion(question, answer));
                                                    questionAdapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                Log.d("QuestionListActivity", "Quiz not found for question: " + questionText);
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("QuestionListActivity", "Error fetching quiz details", e));
                            }
                        }
                    } else {
                        Log.d("QuestionListActivity", "No questions found for this level.");
                    }
                })
                .addOnFailureListener(e -> Log.e("QuestionListActivity", "Error fetching questions", e));
    }

}
