package ict.ihu.gr.loopify;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

// Class representing the artist object
public class Artist extends AppCompatActivity {
    private String name;  // Artist name

    public Artist(String name) {
        this.name = name;
    }

    // Getter
    public String getName() {
        return name;
    }

    Button addSongToLiked, addSongToPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_artist_info);

        addSongToLiked = findViewById(R.id.addToPlaylist);
        addSongToLiked.setOnClickListener(v -> {

        });

    }
}
