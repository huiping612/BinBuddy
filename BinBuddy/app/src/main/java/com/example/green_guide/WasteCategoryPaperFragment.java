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
import com.example.binbuddy.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WasteCategoryPaperFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WasteCategoryPaperFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WasteCategoryPaperFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WasteCategoryPaperFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WasteCategoryPaperFragment newInstance(String param1, String param2) {
        WasteCategoryPaperFragment fragment = new WasteCategoryPaperFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_waste_category_paper, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton IVBackNavArrow = view.findViewById(R.id.BtnBack);
        IVBackNavArrow.setOnClickListener(v -> {
            replaceFragment(new RecyclingTipsFragment());
        });
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace the fragment with the new one
                .addToBackStack(null) // Add to back stack so it can be popped later
                .commit();
    }
}