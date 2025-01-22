package com.example.quiz;

public class QuizResult {
    private String username;
    private String level;
    private String question;
    private String selectedOption;
    private boolean isCorrect;
    private int score;

    public QuizResult(String username, String level, String question, String selectedOption, boolean isCorrect, int score) {
        this.username = username;
        this.level = level;
        this.question = question;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
        this.score = score;
    }

    // Getters and setters for all the fields
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
