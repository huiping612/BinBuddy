package com.example.green_plaza;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.binbuddy.R;
import com.example.binbuddy.UserSessionManager;
import com.example.blog.ImageUtils;
import com.example.point_collection.VoucherFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CheckOutFragment extends Fragment {

    private static final String ARG_ITEM_ID = "itemID"; // Define argument key
    private String itemID; // Store itemID
    private FirebaseFirestore db;
    public static Item item;
    public double price;
    TextView fullNameTextView, fullAddressText, productName, productPrice, totalAmount, voucherAmount;
    ImageView productPic;
    Button addVoucher;

    private double voucherPrice =5.0;

    // Static factory method to create a new instance of CheckOutFragment with arguments
    public static CheckOutFragment newInstance(String itemID) {
        CheckOutFragment fragment = new CheckOutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, itemID); // Pass itemID as an argument
        fragment.setArguments(args); // Attach arguments to the fragment
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            itemID = getArguments().getString(ARG_ITEM_ID); // Retrieve the itemID from arguments
        }

        if (itemID != null) {
            fetchItemData(itemID); // Fetch item details using itemID
        } else {
            Log.e("CheckOutFragment", "itemID is null. Cannot fetch item data.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_out, container, false);

        if (getArguments() != null) {
//            voucherPrice = getArguments().getDouble("voucherPrice", 0.0); // Default to 0 if not passed
            itemID = getArguments().getString(ARG_ITEM_ID);  // Ensure the itemID is correctly retrieved
        }

        // Check if itemID is null and log an error or handle accordingly
        if (itemID == null) {
            Log.e("CheckOutFragment", "itemID is null! Cannot fetch item data.");
            return view; // Optionally, you can return a default view or display a message to the user
        }

        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.itemPrice);
        productPic = view.findViewById(R.id.productImage);
        totalAmount = view.findViewById(R.id.totalAmount);
        voucherAmount = view.findViewById(R.id.voucherPrice);
        fullNameTextView = view.findViewById(R.id.fullNameText);
        fullAddressText = view.findViewById(R.id.fullAddressText);
        addVoucher = view.findViewById(R.id.addVoucher);
        Button submitButton = view.findViewById(R.id.submitButton);

        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String username = sessionManager.getLoggedInUser();
        fetchUserData(username, fullNameTextView, fullAddressText);
        fetchItemData(itemID);

        ImageButton btnBack = view.findViewById(R.id.BtnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        addVoucher.setOnClickListener(v -> {
            VoucherFragment voucherFragment = new VoucherFragment();
            Bundle args = new Bundle();
            args.putString("username", username); // Pass the logged-in username
            voucherFragment.setArguments(args);



            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, voucherFragment)
                    .addToBackStack(null)
                    .commit();
            voucherAmount.setText(String.format("%.2f", voucherPrice));
            calculateTotalPrice();
        });



        Log.d("CheckOutFragment", "Item price: " + price + ", Voucher price: " + voucherPrice);
        submitButton.setOnClickListener(v -> {
            // Delete the item from the "secondHandItem" collection based on voucherId (or itemID)
            db.collection("secondHandItem")
                    .whereEqualTo("itemID", itemID) // Filter by itemID (voucherId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            doc.getReference().delete()  // Delete the document
                                    .addOnSuccessListener(aVoid -> {
                                        // Item deleted successfully
                                        Log.d("VoucherFragment", "Item deleted: " + itemID);

                                        // Show a success message (toast)
                                        Toast.makeText(requireContext(), "You have successfully purchased the item!", Toast.LENGTH_SHORT).show();

                                        navigateToBuyingPage();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("VoucherFragment", "Failed to delete item: " + e.getMessage());
                                        Toast.makeText(requireContext(), "Failed to redeem item. Please try again.", Toast.LENGTH_SHORT).show();

                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("VoucherFragment", "Error fetching item for deletion: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error occurred while processing your request.", Toast.LENGTH_SHORT).show();
                    });
        });


        return view;
    }

    private void fetchUserData(String username, TextView usernameText, TextView addressText) {
        db = FirebaseFirestore.getInstance();

        db.collection("user")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String fetchedUsername = document.getString("username");
                            String fetchedAddress = document.getString("address");

                            usernameText.setText(fetchedUsername);
                            addressText.setText(fetchedAddress);
                        }
                    } else {
                        Log.d("Firestore", "No user found with username: " + username);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user data", e));
    }

    private void fetchItemData(String itemId) {
        db = FirebaseFirestore.getInstance();

        db.collection("secondHandItem")
                .document(itemId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String itemName = document.getString("itemName");
                        Double price = document.getDouble("price");
                        this.price=price;
                        String productImage = document.getString("itemImage");

                        productName.setText(itemName);
                        productPrice.setText(String.format("%.2f", price));
                        totalAmount.setText(String.format("%.2f", price));

                        // Check if the image is a Base64 string or a URL
                        if (productImage != null) {
                            if (isBase64Encoded(productImage)) {
                                try {
                                    Bitmap profileImageBitmap = ImageUtils.decodeBase64ToBitmap(productImage);
                                    productPic.setImageBitmap(profileImageBitmap);
                                } catch (IllegalArgumentException e) {
                                    Log.e("CheckOutFragment", "Invalid Base64 string for product image", e);
                                    productPic.setImageResource(R.drawable.placeholder_image);
                                }
                            } else {
                                Glide.with(requireContext())
                                        .load(productImage)
                                        .placeholder(R.drawable.placeholder_image)
                                        .error(R.drawable.placeholder_image)
                                        .into(productPic);
                            }
                        } else {
                            productPic.setImageResource(R.drawable.placeholder_image);
                        }
                    } else {
                        Log.d("Firestore", "No item found with itemID: " + itemId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching item data", e));
    }

    private boolean isBase64Encoded(String string) {
        try {
            byte[] decodedBytes = Base64.decode(string, Base64.DEFAULT);
            return decodedBytes.length > 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void calculateTotalPrice() {
        double totalAmountAfterDiscount = price - voucherPrice;
        totalAmount.setText(String.format(" %.2f", totalAmountAfterDiscount));
    }

    private void navigateToBuyingPage() {
        // Create a new instance of BuyingPageFragment
        BuyingPageFragment buyingPageFragment = new BuyingPageFragment();

        // Start a new fragment transaction to replace the current fragment with BuyingPageFragment
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, buyingPageFragment); // Replace with the correct container ID
        transaction.addToBackStack(null); // Optional: Add to back stack so user can navigate back to this page
        transaction.commit();
    }
}

