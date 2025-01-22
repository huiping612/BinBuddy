package com.example.quiz;

public class LibraryQuestion {
    private String question;
    private String answer;

    public LibraryQuestion(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}
