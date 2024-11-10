package ict.ihu.gr.loopify.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ict.ihu.gr.loopify.R;
import androidx.appcompat.widget.Toolbar;  // Import Toolbar

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist); // Use the layout for PlaylistActivity

        // Find the toolbar and set it as the support action bar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        // Views for buttons and recycler view
        Button backButton = findViewById(R.id.back_button);
//        Button openMediaPlayerButton = findViewById(R.id.open_media_player_button);
        ImageView selectedImage = findViewById(R.id.selected_image);
        RecyclerView recyclerView = findViewById(R.id.playlist_recycler_view);

        // Set the selected image based on the intent data
        int imageResource = getIntent().getIntExtra("imageResource", R.drawable.background);
        selectedImage.setImageResource(imageResource);

        // Set up RecyclerView for playlist
        String[] songNames = {"Song 1", "Song 2", "Song 3"};
        String[] artistNames = {"Artist 1", "Artist 2", "Artist 3"};
        int[] imageResources = {R.drawable.background, R.drawable.background, R.drawable.background}; // Example image resources

        ict.ihu.gr.loopify.ui.PlaylistAdapter playlistAdapter = new ict.ihu.gr.loopify.ui.PlaylistAdapter(songNames, artistNames, imageResources);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(playlistAdapter);


        // Back button functionality
        backButton.setOnClickListener(v -> finish());
    }
}
