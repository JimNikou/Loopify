package ict.ihu.gr.loopify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.Track;
import ict.ihu.gr.loopify.TrackManager;
import ict.ihu.gr.loopify.ui.PlaylistAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        // Retrieve image resource and genre passed via fragment arguments
        int imageResource = getArguments() != null ? getArguments().getInt("imageResource") : R.drawable.background;
        String genre = getArguments() != null ? getArguments().getString("genre") : "";

        ImageView imageView = root.findViewById(R.id.selected_image);
        imageView.setImageResource(imageResource);

        RecyclerView recyclerView = root.findViewById(R.id.playlist_recycler_view);

        // Initialize TrackManager
        TrackManager trackManager = new TrackManager();

        // Fetch tracks for the genre
        trackManager.fetchTracksForAllGenres(new ict.ihu.gr.loopify.ApiManager(), (fetchedGenre, tracks) -> {
            if (genre.equals(fetchedGenre)) {
                // Prepare data for the adapter
                List<String> songNames = new ArrayList<>();
                List<String> artistNames = new ArrayList<>();
                List<Integer> imageResources = new ArrayList<>();

                for (Track track : tracks) {
                    songNames.add(track.getName());
                    artistNames.add(track.getArtist().getName());
                    imageResources.add(R.drawable.background); // Replace with actual image resource logic
                }

                // Set up adapter with fetched data
                getActivity().runOnUiThread(() -> {
                    PlaylistAdapter adapter = new PlaylistAdapter(
                            songNames.toArray(new String[0]),
                            artistNames.toArray(new String[0]),
                            imageResources.toArray(new String[0])  // Pass image URLs as String[]
                    );
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                });
            }
        });

        // Set up the back button
        Button backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return root;
    }
}
