package com.example.green_guide;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.binbuddy.R;


public class RecyclingTipsFragment extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycling_tips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton IVBackNavArrow = view.findViewById(R.id.BtnBack);
        IVBackNavArrow.setOnClickListener(v -> {
            replaceFragment(new InfoCenter_HomeFragment());
        });

        LinearLayout LLWastePaper = view.findViewById(R.id.LLWastePaper);
        LLWastePaper.setOnClickListener(v->{
            replaceFragment(new WasteCategoryPaperFragment());
        });

        LinearLayout LLWasteGlass = view.findViewById(R.id.LLWasteGlass);
        LLWasteGlass.setOnClickListener(v->{
            replaceFragment(new WasteCategoryGlassFragment());
        });

        LinearLayout LLWastePlastic= view.findViewById(R.id.LLWastePlastic);
        LLWastePlastic.setOnClickListener(v->{
            replaceFragment(new WasteCategoryPlasticFragment());
        });
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace the fragment with the new one
                .addToBackStack(null) // Add to back stack so it can be popped later
                .commit();
    }
}