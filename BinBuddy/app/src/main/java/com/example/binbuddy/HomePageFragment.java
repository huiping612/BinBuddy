package com.example.binbuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blog.ViewingActivity;
import com.example.green_guide.InfoCenter_HomeFragment;
import com.example.green_plaza.PlazaHomeFragment;
import com.example.point_collection.ProfileFragment;
import com.example.quiz.QuizPage;
//import com.example.greenplaza.greenGuide.InfoCenter_HomeFragment;

public class HomePageFragment extends Fragment {

    private DrawerLayout drawer_layout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        if (username == null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
        // Initialize DrawerLayout
        drawer_layout = view.findViewById(R.id.drawer_layout);

        // Navigate to InfoCentreActivity
        view.findViewById(R.id.Module1).setOnClickListener(v -> {

            // Get NavController from the NavHostFragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InfoCenter_HomeFragment()) // replace with SecondHandPlazaFragment
                    .addToBackStack(null) // optional: allows user to go back to previous fragment
                    .commit();
        });

        // Navigate to PointCentreActivity
        view.findViewById(R.id.Module2).setOnClickListener(v -> {

            if (username != null) {
                ProfileFragment profileFragment = new ProfileFragment();

                Bundle args = new Bundle();
                args.putString("username", username);
                profileFragment.setArguments(args);

                // Replace the current fragment with ProfileFragment
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profileFragment) // Replace fragment_container with your actual container ID
                        .addToBackStack(null) // Optional: Allows user to go back to the previous fragment
                        .commit();
            } else {
                // If the user is not logged in, show a message or redirect to login
                Toast.makeText(requireContext(), "You need to log in first", Toast.LENGTH_SHORT).show();
            }
        });
//
        // Navigate to QuizActivity
        view.findViewById(R.id.Module3).setOnClickListener(v -> {
            // Navigate to QuizHubActivity
            if (username != null) {
                Intent intent = new Intent(getActivity(), QuizPage.class);
                intent.putExtra("username", username);  // Pass the username to ViewingActivity
                startActivity(intent);
            } else {
                // If the user is not logged in, show a message or redirect to login
                Toast.makeText(requireContext(), "You need to log in first", Toast.LENGTH_SHORT).show();
            }
       });

        // Navigate to SecondHandPlazaFragment
        view.findViewById(R.id.Module4).setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PlazaHomeFragment()) // replace with SecondHandPlazaFragment
                    .addToBackStack(null) // optional: allows user to go back to previous fragment
                    .commit();
        });

        // Navigate to BlogTimeActivity
        view.findViewById(R.id.Module5).setOnClickListener(v -> {
            if (username != null) {
                Intent intent = new Intent(getActivity(), ViewingActivity.class);
                intent.putExtra("username", username);  // Pass the username to ViewingActivity
                startActivity(intent);
            } else {
                // If the user is not logged in, show a message or redirect to login
                Toast.makeText(requireContext(), "You need to log in first", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

//    @Override
//    public void onBackPressed() {
//        // Close the drawer if it's open when the back button is pressed
//        if (drawer_layout != null && drawer_layout.isDrawerOpen(GravityCompat.END)) {
//            drawer_layout.closeDrawer(GravityCompat.END);
//        } else {
//            super.onBackPressed();
//        }
//    }
}
