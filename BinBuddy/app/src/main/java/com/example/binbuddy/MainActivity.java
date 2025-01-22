package com.example.binbuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.point_collection.ProfileFragment;
import com.example.point_collection.RecycleFormFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.binbuddy.R;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageView bottomNavigationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationButton = findViewById(R.id.center_button);
        bottomNavigationView.setVisibility(View.GONE);
        bottomNavigationButton.setVisibility(View.GONE);

        // Set default fragment to LoginFragment
        if (savedInstanceState == null) {
            replaceFragment(new LoginFragment());
        }

        bottomNavigationButton.setOnClickListener(v -> {
            replaceFragment(new RecycleFormFragment());
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.action_home) {
                selectedFragment = new HomePageFragment();
            } else if (item.getItemId() == R.id.action_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;
        });
    }

    // Method to replace current fragment
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        // Add the transaction to the back stack for navigation
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // This method should be called after successful login
    public void onLoginSuccess() {
        // Show the bottom navigation after login success
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationButton.setVisibility(View.VISIBLE);
        // Optionally, navigate to the home screen or another fragment after login
        replaceFragment(new HomePageFragment());  // Adjust the destination as needed
    }
}
