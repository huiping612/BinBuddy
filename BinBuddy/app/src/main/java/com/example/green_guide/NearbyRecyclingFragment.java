package com.example.green_guide;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.binbuddy.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class NearbyRecyclingFragment extends Fragment {

    private Spinner SPCategory;
    private Button BtnFind;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat = 0, currentLong = 0;
    private FloatingActionButton FABRecycling;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_recycling, container, false);

        // Initialize views
        SPCategory = view.findViewById(R.id.SPCategory);
        BtnFind = view.findViewById(R.id.BtnFind);
        FABRecycling = view.findViewById(R.id.FABRecycling);

        // Initialize SupportMapFragment
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.FragMap);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Setup place category spinner
        String[] placeCategory = {"Recycling Center"};
        SPCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, placeCategory));

        // Check location permissions and get current location
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
        }

        // Button to search nearby recycling centers
        BtnFind.setOnClickListener(v -> searchNearbyRecyclingCenters());
        FABRecycling.setOnClickListener(v -> searchNearbyRecyclingCenters());

        return view;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLat = 3.1390; // Latitude for Kuala Lumpur
                currentLong = 101.6869;
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(googleMap -> {
                        map = googleMap;
                        LatLng curLatLng = new LatLng(currentLat, currentLong);
                        map.addMarker(new MarkerOptions()
                                .position(curLatLng)
                                .title("Current Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 10));
                    });
                }
            }
        });
    }

    private void searchNearbyRecyclingCenters() {
        String apiKey = getResources().getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + currentLat + "," + currentLong
                + "&radius=10000"  // Increased search radius
                + "&keyword=recycling|recycle"  // Added both "recycling" and "recycle" in the search
                + "&key=" + apiKey;

        // Execute PlaceTask to fetch nearby places
        new PlaceTask().execute(url);
    }

    private String downloadUrl(String string) throws IOException {
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("API_RESPONSE", s); // Log the API response
            new ParserTask().execute(s);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String, String>> mapList = null;
            try {
                JSONObject object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            if (map != null) {
                map.clear();
                for (HashMap<String, String> hashMap : hashMaps) {
                    double lat = Double.parseDouble(hashMap.get("lat"));
                    double lng = Double.parseDouble(hashMap.get("lng"));
                    String name = hashMap.get("name");
                    LatLng latLng = new LatLng(lat, lng);
                    map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
            }
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton IVBackNavigationArrow = view.findViewById(R.id.BtnBack);
        IVBackNavigationArrow.setOnClickListener( v ->{
            replaceFragment(new InfoCenter_HomeFragment());
        });
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace the fragment with the new one
                .addToBackStack(null) // Add to back stack so it can be popped later
                .commit();
    }
}
