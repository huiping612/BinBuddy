package com.example.green_guide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.binbuddy.HomePageFragment;
import com.example.binbuddy.R;

public class InfoCenter_HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_center_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.BtnBack);
        btnBack.setOnClickListener(v -> {
            replaceFragment(new HomePageFragment());
        });

        // Show the Bottom Navigation
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        ImageView centerButton = requireActivity().findViewById(R.id.center_button);
        if (bottomNav != null && centerButton != null) {
            bottomNav.setVisibility(View.VISIBLE);
            centerButton.setVisibility(View.VISIBLE);
        }

        // Define click listeners for navigation
        View searchProductCard = view.findViewById(R.id.CVSearchProduct);
        searchProductCard.setOnClickListener(v -> replaceFragment(new SearchProductFragment()));

        ImageView IVRecyclingTips = view.findViewById(R.id.IVRecyclingTips);
        IVRecyclingTips.setOnClickListener(v -> replaceFragment(new RecyclingTipsFragment()));

        ImageView IVMap = view.findViewById(R.id.IVMap);
        IVMap.setOnClickListener(v -> replaceFragment(new NearbyRecyclingFragment()));
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace the fragment with the new one
                .addToBackStack(null) // Add to back stack so it can be popped later
                .commit();
    }

}
