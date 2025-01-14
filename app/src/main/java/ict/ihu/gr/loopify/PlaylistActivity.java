package ict.ihu.gr.loopify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ict.ihu.gr.loopify.ui.PlaylistAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Button backButton = findViewById(R.id.back_button);
        ImageView selectedImage = findViewById(R.id.selected_image);
        RecyclerView recyclerView = findViewById(R.id.playlist_recycler_view);

        // Set the selected image based on the intent data
        int imageResource = getIntent().getIntExtra("imageResource", R.drawable.background);
        selectedImage.setImageResource(imageResource);

        TrackManager trackManager = new TrackManager();
        String genre = getIntent().getStringExtra("genre");

        trackManager.fetchTracksForAllGenres(new ApiManager(), (fetchedGenre, tracks) -> {
            if (genre.equals(fetchedGenre)) {
                List<String> songNames = new ArrayList<>();
                List<String> artistNames = new ArrayList<>();
                List<String> imageUrls = new ArrayList<>();

                for (Track track : tracks) {
                    songNames.add(track.getName());
                    artistNames.add(track.getArtist().getName());
                    if (!track.getImage().isEmpty()) {
                        imageUrls.add(track.getImage().get(0).getText());
                    }
                }

                runOnUiThread(() -> {
                    PlaylistAdapter adapter = new PlaylistAdapter(
                            songNames.toArray(new String[0]),
                            artistNames.toArray(new String[0]),
                            imageUrls.toArray(new String[0]),
                            this::startMusicService // Pass the click listener
                    );

                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                });
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void startMusicService(String songName, String artistName) {
        Intent serviceIntent = new Intent(this, MediaPlayerService.class);
        serviceIntent.setAction("PLAY");
        serviceIntent.putExtra("TRACK_NAME", songName);
        serviceIntent.putExtra("ARTIST_NAME", artistName);
        startService(serviceIntent);

        Log.d("PlaylistActivity", "Started music service with track: " + songName + " by " + artistName);
    }
}
