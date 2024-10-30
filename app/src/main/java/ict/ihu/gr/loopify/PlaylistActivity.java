// PlaylistActivity.java
package ict.ihu.gr.loopify.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ict.ihu.gr.loopify.R;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Button backButton = findViewById(R.id.back_button);
        ImageView selectedImage = findViewById(R.id.selected_image);
        RecyclerView recyclerView = findViewById(R.id.playlist_recycler_view);

        int imageResource = getIntent().getIntExtra("imageResource", R.drawable.background);
        selectedImage.setImageResource(imageResource);

        String[] songNames = {"Song 1", "Song 2", "Song 3"};
        String[] artistNames = {"Artist 1", "Artist 2", "Artist 3"};

        ict.ihu.gr.loopify.ui.PlaylistAdapter playlistAdapter = new ict.ihu.gr.loopify.ui.PlaylistAdapter(songNames, artistNames);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(playlistAdapter);

        backButton.setOnClickListener(v -> finish());
    }
}
