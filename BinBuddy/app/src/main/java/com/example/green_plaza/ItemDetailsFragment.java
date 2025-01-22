package com.example.green_plaza;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.binbuddy.UserSessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.binbuddy.R;

public class ItemDetailsFragment extends BottomSheetDialogFragment {

    private TextView itemName, itemLocation, itemDescription, itemPrice;
    private ImageView itemImage;
    private Button BtnViewCmt, BtnBuyNow;  // Add Buy Now button
    public Item item;
    private FrameLayout commentsContainer;  // Add container for comments
    private FirebaseFirestore db;  // Initialize Firestore

    // Create newInstance to pass the item object
    public static ItemDetailsFragment newInstance(Item item) {
        ItemDetailsFragment fragment = new ItemDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("item", (Parcelable) item);  // Use Parcelable instead of Serializable
        fragment.setArguments(args);
        return fragment;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();

        View view = inflater.inflate(R.layout.fragment_item_details, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the passed Item object
        item = getArguments().getParcelable("item");

        // Initialize views
        itemName = view.findViewById(R.id.item_name);
        itemLocation = view.findViewById(R.id.item_location);
        itemDescription = view.findViewById(R.id.item_description);
        itemPrice = view.findViewById(R.id.item_price);
        itemImage = view.findViewById(R.id.item_image);
        BtnViewCmt = view.findViewById(R.id.BtnViewCmt);
        BtnBuyNow = view.findViewById(R.id.BtnBuyNow);  // Initialize Buy Now button
        commentsContainer = view.findViewById(R.id.content_container);  // Get the container for comments

        if (item != null) {
            itemName.setText(item.getItemName());
            itemLocation.setText(item.getLocation());
            itemDescription.setText(item.getItemDescription());
            itemPrice.setText("RM " + item.getPrice());

            // Load image with Glide
            String imageUrl = item.getItemImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(itemImage);
            } else {
                Glide.with(getContext())
                        .load(R.drawable.placeholder_image)
                        .into(itemImage);
            }
        }

        // Set the View Comments button click listener
        BtnViewCmt.setOnClickListener(v -> {
            if (item != null && item.getItemID() != null) {
                // Pass Firestore document ID (itemID) to the fragment
                ItemCommentFragment commentsFragment = ItemCommentFragment.newInstance(item.getItemID());

                // Replace the current fragment with the ItemCommentFragment
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.content_container, commentsFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.d("ItemDetailsFragment", "Item or Item ID is null");
            }
        });

        // Set Buy Now button click listener
        BtnBuyNow.setOnClickListener(v -> {
            showConfirmationDialog(item);
        });

        return view;
    }

    // Show confirmation dialog
    private void showConfirmationDialog(Item item) {
        new AlertDialog.Builder(getContext())
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to buy this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // If user clicks Yes, proceed with Checkout Fragment
                    CheckOutFragment checkOutFragment = CheckOutFragment.newInstance(item.getItemID());
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, checkOutFragment)
                            .addToBackStack(null)
                            .commit();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // If user clicks No, dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }

    // Function to delete the item from the database (Firestore in this case)
    private void deleteItemFromDatabase() {
        String itemId = item.getItemID();
        Log.d("ItemDetailsFragment", "Deleting item with ID: " + itemId);
        if (item == null) {
            Log.e("ItemDetailsFragment", "Item object is null");
        }
        if (item.getItemID() == null) {
            Log.e("ItemDetailsFragment", "Item ID is null");
        }

        // Example: Delete from Firestore using itemId
        db.collection("secondHandItem")
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Show success message
                    Toast.makeText(getContext(), "Item successfully purchased!", Toast.LENGTH_SHORT).show();
                    // Optionally, dismiss the bottom sheet

                    BuyingPageFragment buyingPageFragment = new BuyingPageFragment(); // Initialize the BuyingPageFragment
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, buyingPageFragment); // Replace current fragment with BuyingPageFragment
                    transaction.addToBackStack(null); // Optionally add to backstack for navigation
                    transaction.commit();

                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("ItemDetailsFragment", "Error deleting item", e);
                    Toast.makeText(getContext(), "Failed to purchase item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}
