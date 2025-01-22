package com.example.blog;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int spacing;

    public GridSpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // Item position
        int spanCount = 2; // Number of columns
        boolean isFirstRow = position < spanCount;

        outRect.left = spacing / 2; // Half-spacing for left
        outRect.right = spacing / 2; // Half-spacing for right
        outRect.bottom = spacing / 2; // Full spacing for bottom

        if (isFirstRow) {
            outRect.top = spacing; // Full spacing for the top of the first row
        } else {
            outRect.top = spacing / 2; // Reduce spacing for subsequent rows
        }
    }
}
