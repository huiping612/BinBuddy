package com.example.green_plaza;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import com.example.binbuddy.R;


public class CategoryAdapter extends BaseAdapter  implements Filterable {

    private Context context;
    private List<Category> originalCategoryList;
    private List<Category> filteredCategoryList;
    private ItemFilter filter;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.originalCategoryList = categoryList;
        this.filteredCategoryList = categoryList;
        this.filter = new ItemFilter();
    }
    @Override
    public int getCount() {
        return filteredCategoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredCategoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_category, parent, false);
        }

        ImageView categoryImage = convertView.findViewById(R.id.itemImage);
        TextView categoryName = convertView.findViewById(R.id.itemName);

        Category category = filteredCategoryList.get(position);

        categoryImage.setImageResource(category.getImageResId());
        categoryName.setText(category.getName());

        Log.d("CategoryAdapter", "Populating item: " + category.getName());

        return convertView;
    }


    @Override
    public Filter getFilter() {
        return filter;
    }
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = originalCategoryList;
                results.count = originalCategoryList.size();
            } else {
                List<Category> filteredList = new ArrayList<>();
                for (Category category : originalCategoryList) {
                    if (category.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(category);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredCategoryList = (List<Category>) results.values;
            notifyDataSetChanged();
        }
    }
}


