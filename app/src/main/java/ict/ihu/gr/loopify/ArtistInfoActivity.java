package ict.ihu.gr.loopify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

public class ArtistInfoActivity extends AppCompatActivity {

    private TextView artistNameTextView;
    private TextView artistLabelTextView;
    private TextView artistGenreTextView;
    private TextView artistMoodTextView;
    private TextView artistWebsiteTextView;
    private TextView artistBiographyTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);

        // Initialize UI components
        artistNameTextView = findViewById(R.id.artistNameTextView);
        artistLabelTextView = findViewById(R.id.artistLabelTextView);
        artistGenreTextView = findViewById(R.id.artistGenreTextView);
        artistMoodTextView = findViewById(R.id.artistMoodTextView);
        artistWebsiteTextView = findViewById(R.id.artistWebsiteTextView);
        artistBiographyTextView = findViewById(R.id.artistBiographyTextView);
        TextView trackInfoTextView = findViewById(R.id.trackInfoTextView);
        // Get the JSON response from the intent
        Intent intent = getIntent();
        String jsonResponse = intent.getStringExtra("artistJson");
        String trackInfoJson = intent.getStringExtra("trackJson");
        // Parse and display artist details
        if (jsonResponse != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONObject artistObject = jsonObject.getJSONArray("artists").getJSONObject(0);
                String trackInfo = ApiManager.parseTrackDetails(trackInfoJson);
                trackInfoTextView.setText(trackInfo);
                artistNameTextView.setText(artistObject.optString("strArtist", "N/A"));
                artistLabelTextView.setText("Label: " + artistObject.optString("strLabel", "N/A"));
                artistGenreTextView.setText("Genre: " + artistObject.optString("strGenre", "N/A"));
                artistMoodTextView.setText("Mood: " + artistObject.optString("strMood", "N/A"));
                artistWebsiteTextView.setText("Website: " + artistObject.optString("strWebsite", "N/A"));
                artistBiographyTextView.setText(artistObject.optString("strBiographyEN", "Biography not available."));
            } catch (JSONException e) {
                e.printStackTrace(); //log file and inform user
            }
        }
    }
}
