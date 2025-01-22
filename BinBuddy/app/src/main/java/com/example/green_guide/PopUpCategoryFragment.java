package com.example.green_guide;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;

public class PopUpCategoryFragment extends Fragment {

    private TextView categoryTitle;
    private ImageView categoryIcon;
    private ImageView categoryImage;
    private AppCompatButton findNearbyBinButton;

    private String categoryName;
    private String binColor;
    private String binIcon;
    private String binImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_pop_up_category, container, false);

        categoryTitle = view.findViewById(R.id.categoryTitle);
        categoryIcon = view.findViewById(R.id.categoryIcon);
        findNearbyBinButton = view.findViewById(R.id.findNearbyBinButton);
        categoryImage = view.findViewById(R.id.categoryImage);

        //get the arguments from previous fragment
        if(getArguments() != null){
            categoryName = getArguments().getString("category_name");
            binColor = getArguments().getString("bin_color");
            binIcon = getArguments().getString("bin_icon");
            binImage = getArguments().getString("category_image");
        }

        //set the details
        categoryTitle.setText(categoryName);
        // Dynamically load the drawable resource
        if (binIcon != null) {
            int resourceId = getResources().getIdentifier(binIcon, "drawable", getContext().getPackageName());
            if (resourceId != 0) { // Check if resource exists
                categoryIcon.setImageResource(resourceId); // Set drawable resource to ImageView
            } else {
                Log.e("PopUpCategoryFragment", "Drawable resource not found for: " + binIcon);
            }
        }

        // Dynamically load the drawable resource for the category image
        if (binImage != null) {
            int imageResourceId = getResources().getIdentifier(binImage, "drawable", getContext().getPackageName());
            if (imageResourceId != 0) { // Check if resource exists
                categoryImage.setImageResource(imageResourceId);
            } else {
                Log.e("PopUpCategoryFragment", "Drawable resource not found for image: " + binImage);
            }
        }

        findNearbyBinButton.setOnClickListener( v->{
               replaceFragment(new NearbyRecyclingFragment());
        });
        return view;
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace the fragment with the new one
                .addToBackStack(null) // Add to back stack so it can be popped later
                .commit();
    }
}