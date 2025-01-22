package com.example.green_plaza;

import android.content.Context;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.widget.SearchView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class BrowsingItemFragment extends Fragment {

    private GridView gridView;
    private ItemAdapter itemAdapter;
    private ArrayList<Item> itemList = new ArrayList<>();
    private FirebaseFirestore db;
    private String categoryId;
    private ProgressBar progressBar;
    private SearchView searchView;

    public static BrowsingItemFragment newInstance(String categoryId) {
        BrowsingItemFragment fragment = new BrowsingItemFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
        }
        db = FirebaseFirestore.getInstance();
        setRetainInstance(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        View view = inflater.inflate(R.layout.fragment_browsing_item, container, false);

        gridView = view.findViewById(R.id.gridView1);
        progressBar = view.findViewById(R.id.progressBar);
        ImageButton btnBack = view.findViewById(R.id.BtnBack2);
        progressBar.setVisibility(View.VISIBLE);

        if (getArguments() != null) {
            String searchQuery = getArguments().getString("searchQuery", "").trim();
            if (!searchQuery.isEmpty()) {
                fetchItemsFromFirestore(searchQuery); // Fetch items with search query
            } else {
                fetchItemsFromFirestore(""); // No search query, fetch all items
            }
        }

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        // Add click listener to open detail page
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

    private void fetchItemsFromFirestore(String searchQuery) {
        db.collection("secondHandItem")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            itemList.clear();
                            for (DocumentSnapshot document : querySnapshot) {
                                String itemName = document.getString("itemName");
                                String itemImage = document.getString("itemImage");
                                Double price = document.getDouble("price");

                                if (itemName != null && !itemName.isEmpty() &&
                                        itemImage != null && !itemImage.isEmpty() &&
                                        price != null) {
                                    Item item = document.toObject(Item.class);
                                    itemList.add(item);
                                }
                            }

                            if (searchQuery.isEmpty()) {
                                updateItemAdapter(itemList); // Show all items if no search query
                            } else {
                                filterItems(searchQuery); // Apply search filter
                            }
                        } else {
                            showToast("No items available");
                        }
                    } else {
                        showToast("Error getting items");
                    }
                });
    }

    public void filterItems(String query) {
        ArrayList<Item> filteredList = new ArrayList<>();
        for (Item item : itemList) {
            if (item.getItemName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        updateItemAdapter(filteredList); // Update the GridView with the filtered items
    }

    private void updateItemAdapter(ArrayList<Item> list) {
        if (itemAdapter == null) {
            itemAdapter = new ItemAdapter(getContext(), list);
            gridView.setAdapter(itemAdapter);
        } else {
            itemAdapter.clear();
            itemAdapter.addAll(list);
            itemAdapter.notifyDataSetChanged();
        }
    }

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
