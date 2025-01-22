package com.example.green_plaza;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;

import com.example.binbuddy.LoginFragment;
import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;

public class PlazaHomeFragment extends Fragment {

    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        if (username == null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchView = view.findViewById(R.id.SVBrowseItem);
        Button sellingButton = view.findViewById(R.id.BSelling);
        Button purchaseButton = view.findViewById(R.id.BPurchase);
        ImageButton btnBack = view.findViewById(R.id.BtnBack2);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Button click listeners for navigating to respective fragments
        sellingButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SellingPageFragment())
                        .addToBackStack(null)
                        .commit()
        );

        purchaseButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BuyingPageFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Set an OnQueryTextListener to the searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchItems(query);  // Call searchItems with the query
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: Handle real-time search if needed
                return false;
            }
        });

        return view;
    }

    // Method to pass the query to BrowsingItemFragment
    private void searchItems(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("searchQuery", query);  // Pass the search query

        BrowsingItemFragment fragment = new BrowsingItemFragment();
        fragment.setArguments(bundle);

        // Replace current fragment with BrowsingItemFragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

