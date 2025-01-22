package com.example.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.binbuddy.R;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class QuizPage extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.quizpage); // Ensure this matches your layout filename

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
        //addQuizQuestions();

        // Find ImageViews (buttons) for leaderboard and library
        ImageView leaderboardButton = findViewById(R.id.leaderboard_button);
        ImageView libraryButton = findViewById(R.id.library_button);
        ImageView backButton= findViewById(R.id.back_button);

        //TextView
        TextView TVQuestion = findViewById(R.id.TVQuestion);
        TVQuestion.setText("Question");

        TextView TVLeaderboard = findViewById(R.id.TVLeaderboard);
        TVLeaderboard.setText("Leaderboard");

        TextView TVLibrary = findViewById(R.id.TVLibrary);
        TVLibrary.setText("Library");

        TextView TVDescription = findViewById(R.id.TVDescription);
        TVDescription.setText("How it works: Participants will encounter multiple-choice");

        // Set an OnClickListener to navigate to Leaderboard
        backButton.setOnClickListener(v -> {
            // Close the current activity and go back to the previous one
            finish();
        });

        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Leaderboard activity
                Intent intent = new Intent(QuizPage.this, Leaderboard.class);
                startActivity(intent);
            }
        });

        // Set an OnClickListener to navigate to Library
        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Library activity
                Intent intent = new Intent(QuizPage.this, Library.class);
                startActivity(intent);
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizPage.this, Question.class);
                intent.putExtra("level", "beginner");
                startActivity(intent);
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizPage.this, Question.class);
                intent.putExtra("level", "intermediate");
                startActivity(intent);
            }
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizPage.this, Question.class);
                intent.putExtra("level", "advanced");
                startActivity(intent);
            }
        });
    }

    private void addQuizQuestions() {
        // Example quiz questions with levels
        List<QuizQuestion> questions = new ArrayList<>();

        questions.add(new QuizQuestion(
                "Which material is most commonly recycled worldwide?",
                "Paper",
                "Plastic",
                "Glass",
                "Aluminum",
                "A", // Correct option: "Paper"
                "beginner" // Level: "beginner"
        ));

        questions.add(new QuizQuestion(
                "What is a major challenge in recycling plastic?",
                "It can only be recycled once",
                "Plastic is difficult to identify",
                "It’s too expensive to recycle",
                "Most plastics are not recyclable",
                "B", // Correct option: "Plastic is difficult to identify"
                "beginner" // Level: "beginner"
        ));

        questions.add(new QuizQuestion(
                "What is one benefit of using recycled paper?",
                "It requires more energy to produce",
                "It reduces deforestation",
                "It cannot be recycled again",
                "It is always more expensive",
                "B", // Correct option: "It reduces deforestation"
                "intermediate" // Level: "intermediate"
        ));

        questions.add(new QuizQuestion(
                "Which of the following items can be recycled?",
                "Used coffee filters",
                "Plastic utensils",
                "Cardboard boxes",
                "Dirty pizza boxes",
                "C", // Correct option: "Cardboard boxes"
                "intermediate" // Level: "intermediate"
        ));

        questions.add(new QuizQuestion(
                "What is the impact of recycling on climate change?",
                "It increases carbon emissions",
                "It has no effect",
                "It reduces carbon footprint",
                "It accelerates global warming",
                "C", // Correct option: "It reduces carbon footprint"
                "advanced" // Level: "advanced"
        ));

        questions.add(new QuizQuestion(
                "Which of the following is NOT a recyclable material?",
                "Old newspapers",
                "Glass bottles",
                "Plastic shopping bags",
                "Aluminum cans",
                "C", // Correct option: "Plastic shopping bags"
                "advanced" // Level: "advanced"
        ));

        // Loop through the questions list and add each question to Firestore
        for (QuizQuestion question : questions) {
            db.collection("quiz").add(question)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error adding document", e);
                    });
        }
    }



}



