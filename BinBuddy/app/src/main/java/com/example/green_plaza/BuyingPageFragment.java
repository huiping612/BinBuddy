package com.example.green_plaza;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.binbuddy.UserSessionManager;
import com.example.green_plaza.CategoryAdapter;
import com.example.binbuddy.R;

import java.util.ArrayList;
import java.util.List;

public class BuyingPageFragment extends Fragment {

    private GridView gridView1;
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        View view = inflater.inflate(R.layout.fragment_buying_page, container, false);

        gridView1 = view.findViewById(R.id.gridView1);
        SearchView searchView = view.findViewById(R.id.SVBrowseItem);
        ImageButton btnBack = view.findViewById(R.id.BtnBack2);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Create sample data for categories
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("Clothes", R.drawable.clothes));
        categoryList.add(new Category("Books", R.drawable.books));
        categoryList.add(new Category("Gadgets", R.drawable.gadgets));
        categoryList.add(new Category("Toys", R.drawable.toys));
        categoryList.add(new Category("Furniture", R.drawable.furniture));

        // Set adapter for grid view
        adapter = new CategoryAdapter(getContext(), categoryList);
        gridView1.setAdapter(adapter);

        // Set item click listener for the grid
        gridView1.setOnItemClickListener((parent, view1, position, id) -> {
            Category selectedCategory = categoryList.get(position);

            CategoryItemFragment categoryItemFragment = CategoryItemFragment.newInstance(selectedCategory.getName());

            // Ensure you're replacing the container and not adding the fragment on top
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, categoryItemFragment); // Replace the correct container
            transaction.addToBackStack(null); // Optionally add to back stack
            transaction.commit();
        });
        // Add query listener to the SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // You can do any action on submit if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Trigger filtering based on the input text
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        Log.d("BuyingPageFragment", "Category List Size: " + categoryList.size());
        return view;
    }
}
