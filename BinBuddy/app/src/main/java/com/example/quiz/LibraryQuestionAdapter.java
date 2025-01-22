package com.example.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.binbuddy.R;

import java.util.List;

public class LibraryQuestionAdapter extends RecyclerView.Adapter<LibraryQuestionAdapter.LibraryQuestionViewHolder> {

    private final List<LibraryQuestion> questionList;

    public LibraryQuestionAdapter(List<LibraryQuestion> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public LibraryQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new LibraryQuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryQuestionViewHolder holder, int position) {
        LibraryQuestion question = questionList.get(position);
        holder.questionText.setText(question.getQuestion());
        holder.answerText.setText(question.getAnswer());
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    static class LibraryQuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText, answerText;

        public LibraryQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            answerText = itemView.findViewById(R.id.answerText);
        }
    }
}

