package com.example.green_guide;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.binbuddy.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;


public class CategoryItemFragment extends Fragment {


    public CategoryItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_item, container, false);

        TextView tvCategoryName = view.findViewById(R.id.TVCategoryName);
        TextView tvItemList = view.findViewById(R.id.TVItemList);

        // Retrieve the category name from the bundle
        String categoryName = getArguments().getString("category");

        if (categoryName == null || categoryName.isEmpty()) {
            Log.e("CategoryItemFragment", "Category name is null or empty.");
            tvItemList.setText("Invalid category.");
            return view;
        }

        Log.d("CategoryItemFragment", "Category name from bundle: " + categoryName);



        tvCategoryName.setText(categoryName);
        // Map the category name to its corresponding Firestore document ID
        Map<String, String> categoryToIdMap = new HashMap<>();
        categoryToIdMap.put("Plastic", "1");
        categoryToIdMap.put("Paper", "2");
        categoryToIdMap.put("Glass", "3");

        String documentId = categoryToIdMap.get(categoryName);

        if (documentId != null) {
            // Query Firestore using the mapped document ID
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("recyclingCategory")
                    .document(documentId) // Use the mapped document ID
                    .collection("products")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            StringBuilder items = new StringBuilder();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String productName = document.getString("product_name");
                                items.append(productName).append("\n");
                            }
                            tvItemList.setText(items.toString()); // Set the items in TextView
                        } else {
                            Log.w("Firestore", "Error getting products.", task.getException());
                            tvItemList.setText("Error loading items.");
                        }
                    });
        } else {
            Log.e("CategoryItemFragment", "Invalid category name: " + categoryName);
            tvItemList.setText("Invalid category.");
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton IVBackNavArrow = view.findViewById(R.id.BtnBack);
        IVBackNavArrow.setOnClickListener(v -> {
            replaceFragment(new SearchProductFragment());
        });

        AppCompatButton BtnFindNearby = view.findViewById(R.id.findNearbyBinButton);
        BtnFindNearby.setOnClickListener(v->{
            replaceFragment(new NearbyRecyclingFragment());
        });
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace the fragment with the new one
                .addToBackStack(null) // Add to back stack so it can be popped later
                .commit();
    }

}