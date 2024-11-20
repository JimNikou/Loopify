package ict.ihu.gr.loopify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ict.ihu.gr.loopify.ApiManager;
import ict.ihu.gr.loopify.PlaylistActivity;
import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.TrackManager;
import ict.ihu.gr.loopify.Track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageCarouselFragment extends Fragment implements ImageCarouselAdapter.OnImageClickListener {

    private int[] imageResources = {
            R.drawable.hiphop_photo,
            R.drawable.house_photo,
            R.drawable.pop_photo,
            R.drawable.jazz_photo,
            R.drawable.rap_photo,
            R.drawable.rock_photo,
            R.drawable.techno_photo
    };

    private String[] genres = {
            "hip hop", "house", "pop", "jazz", "rap", "rock", "techno"
    };

    private String[] artistNames; // Dynamically generated artist names
    private TrackManager trackManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_image_carousel, container, false);

        // Initialize TrackManager
        trackManager = new TrackManager();

        // Set up RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initially set artist names to empty strings
        artistNames = new String[genres.length];
        for (int i = 0; i < artistNames.length; i++) {
            artistNames[i] = "Loading..."; // Placeholder
        }

        // Set up adapter with placeholder data
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(imageResources, artistNames, this);
        recyclerView.setAdapter(adapter);

        // Fetch tracks for all genres asynchronously
        trackManager.fetchTracksForAllGenres(new ApiManager(), (genre, tracks) -> {
            Log.d("CarouselFragment", "Tracks fetched for genre: " + genre);

            // Update the artist names when data is fetched
            int index = -1;
            for (int i = 0; i < genres.length; i++) {
                if (genres[i].equals(genre)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                StringBuilder topArtists = new StringBuilder();
                Set<String> uniqueArtists = new HashSet<>(); // To track unique artist names

                for (int i = 0, addedCount = 0; i < tracks.size() && addedCount < 3; i++) {
                    String artistName = tracks.get(i).getArtist().getName();
                    if (uniqueArtists.add(artistName)) { // Only add if it's not already in the set
                        if (topArtists.length() > 0) topArtists.append(", ");
                        topArtists.append(artistName);
                        addedCount++;
                    }
                }

                artistNames[index] = topArtists.toString();

                // Notify the adapter that data has changed
                getActivity().runOnUiThread(adapter::notifyDataSetChanged);
            }
        });

        return root;
    }


    @Override
    public void onImageClick(int position, int imageResource) {
        // Get the genre for the clicked image
        String genre = genres[position];

        // Start the PlaylistActivity with the corresponding genre
        Intent intent = new Intent(getActivity(), PlaylistActivity.class);
        intent.putExtra("imageResource", imageResource);
        intent.putExtra("genre", genre); // Pass the genre dynamically
        startActivity(intent);
    }
}
