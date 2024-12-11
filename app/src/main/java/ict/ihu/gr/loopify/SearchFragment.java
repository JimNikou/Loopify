package ict.ihu.gr.loopify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    private static SearchFragment instance;
    private static final String TAG = "SearchFragment";
    private Button testButton;
    private EditText searchBar;
    private RecyclerView songListRecyclerView;
    private TrackAdapter trackAdapter;
    private List<Track> tracks = new ArrayList<>();
    private MainActivity mainActivity;

    public static SearchFragment getInstance() {
        if (instance == null) {
            instance = new SearchFragment();
        }
        return instance;
    }

    public void setData(String data) {
        Log.d(TAG, "Setting data: " + data);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.searchBar);
        testButton = view.findViewById(R.id.testbutton);
        songListRecyclerView = view.findViewById(R.id.song_list);

        // Setup RecyclerView
        trackAdapter = new TrackAdapter(requireContext(), tracks, track -> {
            // Log the track details on click
            Log.d(TAG, "Track clicked: " + track.getName() + " by " + track.getArtist().getName());

            // Pass the track information to the service
            callStartMusicService("PLAY", track);
        });

        songListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        songListRecyclerView.setAdapter(trackAdapter);

        // Search button functionality
        testButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString();
            if (!query.isEmpty()) {
                searchForTracks(query);
            } else {
                Toast.makeText(getContext(), "Please enter a track name", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void searchForTracks(String trackName) {
        String apiKey = "a38306489162f067667f1b911c8345c5"; // Replace with your API key

        // Create Retrofit API instance
        TrackSearchApi api = RetrofitClient.getInstance().create(TrackSearchApi.class);
        Call<TrackSearchResponse> call = api.searchTracks(trackName, apiKey);

        // Execute API call asynchronously
        call.enqueue(new Callback<TrackSearchResponse>() {
            @Override
            public void onResponse(Call<TrackSearchResponse> call, Response<TrackSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TrackSearchResponse.Results.TrackMatches.Track> fetchedTracks = response.body().results.trackmatches.track;

                    if (fetchedTracks != null && !fetchedTracks.isEmpty()) {
                        List<Track> appTracks = convertToTracks(fetchedTracks); // Convert API tracks to app tracks

                        // Use a HashSet to store track names and ensure uniqueness (case insensitive)
                        Set<String> trackNamesSet = new HashSet<>();
                        List<Track> uniqueTracks = new ArrayList<>();

                        for (Track track : appTracks) {
                            // Convert track name to lowercase for case-insensitive comparison
                            String trackNameLower = track.getName().toLowerCase();

                            // Only add the track if it's not already in the set
                            if (!trackNamesSet.contains(trackNameLower)) {
                                trackNamesSet.add(trackNameLower);
                                uniqueTracks.add(track);
                            }
                        }

                        // Clear the existing tracks and add the unique ones
                        tracks.clear();
                        tracks.addAll(uniqueTracks);
                        trackAdapter.notifyDataSetChanged(); // Update RecyclerView

                        displayTracks(fetchedTracks); // Log the tracks
                    } else {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "No tracks found for query: " + trackName);
                    }
                } else {
                    Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API response failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TrackSearchResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private List<Track> convertToTracks(List<TrackSearchResponse.Results.TrackMatches.Track> apiTracks) {
        List<Track> appTracks = new ArrayList<>();
        for (TrackSearchResponse.Results.TrackMatches.Track apiTrack : apiTracks) {
            Track track = new Track();
            track.setName(apiTrack.name); // Map the name
            track.setArtist(new Artist(apiTrack.artist)); // Create and map the artist
            appTracks.add(track);
        }
        return appTracks;
    }

    private void displayTracks(List<TrackSearchResponse.Results.TrackMatches.Track> tracks) {
        // Log tracks in Logcat
        for (TrackSearchResponse.Results.TrackMatches.Track track : tracks) {
            Log.d(TAG, "Track: " + track.name + " by " + track.artist);
        }
    }

    private void callStartMusicService(String action, Track track) {
        if (getActivity() instanceof MainActivity) {
            Intent serviceIntent = new Intent(getActivity(), MediaPlayerService.class);
            serviceIntent.setAction(action);
            serviceIntent.putExtra("TRACK_NAME", track.getName()); // Pass track name as extra
            getActivity().startService(serviceIntent);

            Log.d(TAG, "Music service started with track: " + track.getName());
        } else {
            Log.e(TAG, "MainActivity is not attached");
        }
    }

}