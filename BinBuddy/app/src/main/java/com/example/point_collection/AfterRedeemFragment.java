package com.example.point_collection;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.binbuddy.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AfterRedeemFragment extends Fragment {

    private static final String ARG_VOUCHER_ID = "voucherId";
    private static final String ARG_TITLE = "title";
    private String voucherId;
    private String title;
    private FirebaseFirestore db;

    private OnRedeemConfirmListener listener;

    public static AfterRedeemFragment newInstance(String voucherId, String title) {
        AfterRedeemFragment fragment = new AfterRedeemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VOUCHER_ID, voucherId);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            voucherId = getArguments().getString(ARG_VOUCHER_ID);
            title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        View view = inflater.inflate(R.layout.fragment_after_redeem, container, false);

        // Find the TextView where the voucher details will be displayed
        TextView voucherText = view.findViewById(R.id.voucher_text);

        if (voucherId != null && !voucherId.isEmpty()) {
            db.collection("voucher")
                    .document(voucherId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String title = task.getResult().getString("title");
                            String description = task.getResult().getString("description");
                            Timestamp expiryDate = task.getResult().getTimestamp("expiryDate");

                            if (expiryDate != null) {
                                Date modifiedExpiryDate = expiryDate.toDate();
                                String formattedDate = formatDate(modifiedExpiryDate);
                                voucherText.setText(title + "\n" + description + "\nValid Till: " + formattedDate);
                            }
                        } else {
                            Log.w("VoucherFragment", "Failed to fetch voucher details: " + task.getException());
                        }
                    });
        } else {
            Log.w("VoucherFragment", "Invalid voucherId: " + voucherId);
        }

        Button confirmButton = view.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRedeemConfirmed(voucherId);
            } else {
                Log.w("AfterRedeemFragment", "Listener is null");
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    public void setOnRedeemConfirmListener(OnRedeemConfirmListener listener) {
        this.listener = listener;
    }

    public interface OnRedeemConfirmListener {
        void onRedeemConfirmed(String voucherId);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

}
