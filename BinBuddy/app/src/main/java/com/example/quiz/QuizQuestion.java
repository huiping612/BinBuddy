package com.example.quiz;

public class QuizQuestion {
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String level; // Add this line to store the level of the question

    // Empty constructor (required for Firestore)
    public QuizQuestion() {}

    // Constructor with level parameter
    public QuizQuestion(String question, String optionA, String optionB, String optionC, String optionD, String correctOption, String level) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.level = level; // Initialize the level
    }

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public String getLevel() { return level; } // Add getter for level
    public void setLevel(String level) { this.level = level; } // Add setter for level
}
