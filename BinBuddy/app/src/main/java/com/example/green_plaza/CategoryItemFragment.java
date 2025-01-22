package com.example.green_plaza;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.binbuddy.R;

import com.bumptech.glide.Glide;
import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryItemFragment extends Fragment {
    private static final String ARG_CATEGORY_ID = "categoryId";
    private GridView gridView;
    private ItemAdapter itemAdapter;
    private ArrayList<Item> itemList = new ArrayList<>();
    private FirebaseFirestore db;
    private String categoryId;

    private ProgressBar progressBar;

        public static CategoryItemFragment newInstance(String categoryId) {
            CategoryItemFragment fragment = new CategoryItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            Log.d("CategoryId", "Category selected: " + categoryId);
        }
        db = FirebaseFirestore.getInstance();
        // Retain fragment instance across configuration changes
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        View view = inflater.inflate(R.layout.fragment_browsing_item, container, false);

        // Initialize GridView and ProgressBar
        gridView = view.findViewById(R.id.gridView1);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE); // Show progress bar while loading
        ImageButton btnBack = view.findViewById(R.id.BtnBack2);

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Disable intercepting touch events to allow clicking outside to close
        view.setOnTouchListener((v, event) -> false); // Return false to not consume touch events

        // Fetch items from Firestore only if the list is empty
        fetchItemsFromFirestore();

        // In BrowsingItemFragment.java

        gridView.setOnItemClickListener((parent, v, position, id) -> {
            // Get the clicked item from the list
            Item clickedItem = itemList.get(position);

            // Pass item details to ItemDetailsFragment
            ItemDetailsFragment itemDetailsFragment = ItemDetailsFragment.newInstance(clickedItem);

            // Get the container to place the item details fragment
            FrameLayout itemContainer = getView().findViewById(R.id.itemDetailsContainer);

            // Begin fragment transaction
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.itemDetailsContainer, itemDetailsFragment); // Replace fragment in the container
            transaction.addToBackStack(null); // Add the transaction to back stack for navigation
            transaction.commit();

            // Show the container and overlay the details
            itemContainer.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void fetchItemsFromFirestore() {
        if (itemList.isEmpty()) { // Only fetch data if the list is empty
            db.collection("secondHandItem")
                    .whereEqualTo("itemCategory", categoryId) // Filter by category
                    .get()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE); // Hide progress bar once data is fetched

                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                itemList.clear(); // Clear existing list to avoid duplicates
                                for (DocumentSnapshot document : querySnapshot) {
                                    String itemName = document.getString("itemName");
                                    String itemImage = document.getString("itemImage");
                                    Double price = document.getDouble("price");

                                    if (itemName != null && !itemName.isEmpty() &&
                                            itemImage != null && !itemImage.isEmpty() &&
                                            price != null) {
                                        Item item = document.toObject(Item.class);
                                        itemList.add(item);
                                    } else {
                                        Log.e("Firestore", "Invalid item data: " + document.getId());
                                    }
                                }

                                // Set adapter only if new data was fetched
                                if (itemAdapter == null) {
                                    itemAdapter = new ItemAdapter(getContext(), itemList);
                                    gridView.setAdapter(itemAdapter);
                                } else {
                                    itemAdapter.notifyDataSetChanged();
                                }

                                if (itemList.isEmpty()) {
                                    showToast("No available items in this category");
                                }
                            } else {
                                showToast("No items available in this category");
                            }
                        } else {
                            Log.e("Firestore", "Error getting items: ", task.getException());
                            showToast("Error getting items");
                        }
                    });
        }
    }

    // Helper method to show a Toast message
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Adapter to bind data to GridView
    private class ItemAdapter extends ArrayAdapter<Item> {

        public ItemAdapter(Context context, ArrayList<Item> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
                holder = new ViewHolder();
                holder.nameTextView = convertView.findViewById(R.id.itemName);
                holder.priceTextView = convertView.findViewById(R.id.itemPrice);
                holder.imageView = convertView.findViewById(R.id.itemImage);
                convertView.setTag(holder); // Save the ViewHolder to avoid finding views repeatedly
            } else {
                holder = (ViewHolder) convertView.getTag(); // Reuse the existing ViewHolder
            }

            Item currentItem = getItem(position);
            holder.nameTextView.setText(currentItem.getItemName());
            holder.priceTextView.setText("RM " + currentItem.getPrice());

            // Ensure the image URL is valid before loading it with Glide
            String imageUrl = currentItem.getItemImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Load the image from the URL
                Glide.with(getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)  // Show a placeholder if the image is not available
                        .error(R.drawable.placeholder_image)       // Show an error image if Glide fails
                        .into(holder.imageView);
            } else {
                // Load placeholder image if URL is invalid or missing
                Glide.with(getContext())
                        .load(R.drawable.placeholder_image)  // Load a default image in case URL is missing or empty
                        .into(holder.imageView);
            }

            return convertView;
        }


        // ViewHolder class to hold references to the views
        class ViewHolder {
            TextView nameTextView;
            TextView priceTextView;
            ImageView imageView;
        }
    }

}