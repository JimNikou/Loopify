package ict.ihu.gr.loopify;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.TrackManager;
import ict.ihu.gr.loopify.Track;
import ict.ihu.gr.loopify.ui.PlaylistAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        // Views for buttons and recycler view
        Button backButton = findViewById(R.id.back_button);
        ImageView selectedImage = findViewById(R.id.selected_image);
        RecyclerView recyclerView = findViewById(R.id.playlist_recycler_view);

        // Set the selected image based on the intent data
        int imageResource = getIntent().getIntExtra("imageResource", R.drawable.background);
        selectedImage.setImageResource(imageResource);

        // Initialize TrackManager
        TrackManager trackManager = new TrackManager();

        // Get the genre passed from the intent
        String genre = getIntent().getStringExtra("genre");

        // Fetch data for the genre
        trackManager.fetchTracksForAllGenres(new ict.ihu.gr.loopify.ApiManager(), (fetchedGenre, tracks) -> {
            if (genre.equals(fetchedGenre)) {
                // Prepare data for the adapter
                List<String> songNames = new ArrayList<>();
                List<String> artistNames = new ArrayList<>();
                List<String> imageResources = new ArrayList<>();  // Keep as Strings for URLs

                for (Track track : tracks) {
                    songNames.add(track.getName());
                    artistNames.add(track.getArtist().getName());

                    // If track.getImage() contains image URLs (Strings)
                    if (!track.getImage().isEmpty()) {
                        imageResources.add(track.getImage().get(0).getText());  // Add image URL
                    }
                }

                // Set up adapter with fetched data
                runOnUiThread(() -> {
                    PlaylistAdapter playlistAdapter = new PlaylistAdapter(
                            songNames.toArray(new String[0]),
                            artistNames.toArray(new String[0]),
                            imageResources.toArray(new String[0])  // Pass image URLs as String[]
                    );
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(playlistAdapter);
                });
            }
        });

        // Back button functionality
        backButton.setOnClickListener(v -> finish());
    }
}
