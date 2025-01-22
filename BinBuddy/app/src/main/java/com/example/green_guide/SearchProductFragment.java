package com.example.green_guide;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.example.binbuddy.R;

public class SearchProductFragment extends Fragment {

    private EditText ETSearchBar;
    private Button btnSearchProduct;


    // Firestore instance
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SearchProductFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_product, container, false);

        ETSearchBar = view.findViewById(R.id.ETSearchBar);
        btnSearchProduct = view.findViewById(R.id.btnSearchProduct);

        // Setup listener for "ENTER" key press
        btnSearchProduct.setOnClickListener(v -> {

            String productName = ETSearchBar.getText().toString().trim();
            if (!productName.isEmpty()) {
                searchProductInFirestore(productName);
            } else {
                Toast.makeText(getContext(), "Please enter a product name", Toast.LENGTH_SHORT).show();
            }
        });

        // Find the CardViews by ID
        CardView cardPaper = view.findViewById(R.id.CVCategoryPaper);
        CardView cardGlass = view.findViewById(R.id.CVCategoryGlass);
        CardView cardPlastic = view.findViewById(R.id.CVCategoryPlastic);

        // Create a map to associate category names with document IDs
        Map<String, String> categoryToIdMap = new HashMap<>();
        categoryToIdMap.put("Plastic", "1");
        categoryToIdMap.put("Paper", "2");
        categoryToIdMap.put("Glass", "3");

        // Set onClickListeners for each CardView
        cardPaper.setOnClickListener(v -> navigateToCategory("Paper",categoryToIdMap));
        cardGlass.setOnClickListener(v -> navigateToCategory("Glass", categoryToIdMap));
        cardPlastic.setOnClickListener(v -> navigateToCategory("Plastic", categoryToIdMap));

        return view;
    }

    private void navigateToCategory(String categoryName, Map<String, String> categoryToIdMap) {
        String documentId = categoryToIdMap.get(categoryName); // Get the document ID for the category
        if (documentId != null) {
            // Create a new instance of the target fragment
            CategoryItemFragment categoryItemFragment = new CategoryItemFragment();

            // Pass the arguments using a Bundle
            Bundle bundle = new Bundle();
            bundle.putString("category", categoryName);
            categoryItemFragment.setArguments(bundle);

            // Replace the fragment
            replaceFragment(categoryItemFragment);
        } else {
            Log.e("MainFragment", "Invalid category name: " + categoryName);
        }
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment) // Ensure fragment_container matches your FrameLayout ID
                .commit();
    }



    private void searchProductInFirestore(String productName) {
        Log.d("FirestoreDebug", "Searching for product: " + productName);

        db.collection("recyclingCategory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        AtomicBoolean productFound = new AtomicBoolean(false);

                        for (QueryDocumentSnapshot categoryDoc : task.getResult()) {
                            String categoryId = categoryDoc.getId();
                            String categoryName = categoryDoc.getString("category_name");
                            String binColor = categoryDoc.getString("bin_color");
                            String binIcon = categoryDoc.getString("bin_icon");
                            String categoryImage = categoryDoc.getString("category_image");

                            Log.d("FirestoreDebug", "Checking category: " + categoryName);

                            db.collection("recyclingCategory")
                                    .document(categoryId)
                                    .collection("products")
                                    .whereEqualTo("product_name", productName)
                                    .get()
                                    .addOnCompleteListener(productTask -> {
                                        if (productTask.isSuccessful() && !productTask.getResult().isEmpty()) {
                                            productFound.set(true);

                                            for (QueryDocumentSnapshot productDoc : productTask.getResult()) {
                                                Log.d("FirestoreDebug", "Product found in category: " + categoryName);

                                                // Navigate to the pop-up fragment
                                                navigateToPopUpCategoryFragment(categoryName, binColor, binIcon, categoryImage);
                                            }
                                        } else {
                                            Log.d("FirestoreDebug", "Product not found in category: " + categoryName);
                                        }
                                    });
                        }

                        // Check if no product was found after all categories
                        new android.os.Handler().postDelayed(() -> {
                            if (!productFound.get()) {
                                Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
                            }
                        }, 1000); // Delay to allow all queries to finish
                    } else {
                        Toast.makeText(getContext(), "Error fetching categories", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error querying categories: ", e);
                    Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                });
    }


    private void navigateToPopUpCategoryFragment(String categoryName, String binColor, String binIcon, String categoryImage) {
        // Create a new instance of the target fragment
        PopUpCategoryFragment popUpCategoryFragment = new PopUpCategoryFragment();

        // Pass data to the fragment via Bundle
        Bundle bundle = new Bundle();
        bundle.putString("category_name", categoryName);
        bundle.putString("bin_color", binColor);
        bundle.putString("bin_icon", binIcon);
        bundle.putString("category_image", categoryImage);
        popUpCategoryFragment.setArguments(bundle);

        // Replace the current fragment
        replaceFragment(popUpCategoryFragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton IVBackNavArrow = view.findViewById(R.id.IBBack);
        IVBackNavArrow.setOnClickListener(v -> {
            // Navigate back to Home Info Center
            replaceFragment(new InfoCenter_HomeFragment());
        });

    }
}
