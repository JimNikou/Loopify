package ict.ihu.gr.loopify;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackManager {

    // Lists for each genre
    private List<Track> hipHopTracks = new ArrayList<>();
    private List<Track> houseTracks = new ArrayList<>();
    private List<Track> popTracks = new ArrayList<>();
    private List<Track> jazzTracks = new ArrayList<>();
    private List<Track> rapTracks = new ArrayList<>();
    private List<Track> rockTracks = new ArrayList<>();
    private List<Track> technoTracks = new ArrayList<>();

    private Map<String, List<String>> genreArtists = new HashMap<>();


    // Define a listener to pass results back to MainActivity
    public interface OnTracksFetchedListener {
        void onTracksFetched(String genre, List<Track> tracks);
    }

    // Constructor
    public TrackManager() {}

    // Fetch tracks for all genres
    public void fetchTracksForAllGenres(ApiManager apiManager, OnTracksFetchedListener listener) {
        // Songs for each genre
        String[][] genreSongs = {
                {"hip hop", "Dr. Dre", "Nuthin' but a \"G\" Thang"},
                {"house", "Daft Punk", "One More Time"},
                {"pop", "Britney Spears", "Toxic"},
                {"jazz", "Miles Davis", "So What"},
                {"rap", "Kendrick Lamar", "HUMBLE."},
                {"rock", "Nirvana", "Smells Like Teen Spirit"},
                {"techno", "The Chemical Brothers", "Go"}
        };

        // Loop through genres and fetch tracks
        for (String[] genreSong : genreSongs) {
            String genre = genreSong[0];
            String artist = genreSong[1];
            String song = genreSong[2];

            fetchSimilarTracks(apiManager, artist, song, genre, listener);
        }
    }

    // Fetch tracks for a specific genre and song
    private void fetchSimilarTracks(ApiManager apiManager, String artist, String song, String genre, OnTracksFetchedListener listener) {
        apiManager.fetchSimilarTracksRAW(artist, song, new ApiManager.ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                if (jsonResponse != null) {
                    // Initialize Gson with custom deserializer
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Image.class, new ImageDeserializer()) // Register custom deserializer for Image class
                            .create();

                    try {
                        // Use TypeToken to specify the exact type (List<Track>) for Gson to parse
                        TypeToken<List<Track>> typeToken = new TypeToken<List<Track>>() {};
                        List<Track> tracks = gson.fromJson(jsonResponse, typeToken.getType());

                        if (tracks != null && !tracks.isEmpty()) {
                            // Add tracks to the appropriate genre list
                            addTracksToGenre(genre, tracks);
                            listener.onTracksFetched(genre, tracks);  // Notify MainActivity with the genre and tracks
                        } else {
                            Log.d("TrackManager", "No tracks found for " + genre);
                        }
                    } catch (Exception e) {
                        Log.e("TrackManager", "Error while parsing JSON: " + e.getMessage());
                    }
                } else {
                    Log.d("TrackManager", "No similar tracks found for " + artist + " - " + song);
                }
            }
        });
    }

    // Add fetched tracks to the respective genre list
    private void addTracksToGenre(String genre, List<Track> tracks) {
        switch (genre) {
            case "hip hop":
                hipHopTracks.addAll(tracks);
                break;
            case "house":
                houseTracks.addAll(tracks);
                break;
            case "pop":
                popTracks.addAll(tracks);
                break;
            case "jazz":
                jazzTracks.addAll(tracks);
                break;
            case "rap":
                rapTracks.addAll(tracks);
                break;
            case "rock":
                rockTracks.addAll(tracks);
                break;
            case "techno":
                technoTracks.addAll(tracks);
                break;
            default:
                Log.d("TrackManager", "Unknown genre: " + genre);
                break;
        }
    }

    // Get the tracks for a specific genre
    public List<Track> getTracksForGenre(String genre) {
        switch (genre) {
            case "hip hop":
                return hipHopTracks;
            case "house":
                return houseTracks;
            case "pop":
                return popTracks;
            case "jazz":
                return jazzTracks;
            case "rap":
                return rapTracks;
            case "rock":
                return rockTracks;
            case "techno":
                return technoTracks;
            default:
                return new ArrayList<>();
        }
    }
}
