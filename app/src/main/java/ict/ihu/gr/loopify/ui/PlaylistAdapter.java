package ict.ihu.gr.loopify.ui;

import ict.ihu.gr.loopify.FakeMusicAPI;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ict.ihu.gr.loopify.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.SongViewHolder> {
    private final List<String> playlist;  // Store only song names

    public PlaylistAdapter(List<String> playlist) {
        this.playlist = playlist;  // Initialize with song names
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each song item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        // Get the song name at the current position
        String songName = playlist.get(position);

        // Call the fake API to get artist details for the song
        String[] songDetails = FakeMusicAPI.getSongDetails(songName);  // Returns artist, album, genre

        // Set the song name and artist name in the view
        holder.songName.setText(songName);
        holder.artistName.setText(songDetails[0]);  // Set artist name (first element of the fake API result)
    }

    @Override
    public int getItemCount() {
        return playlist.size();  // Return the number of songs
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        // Views for song name and artist name
        TextView songName, artistName;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind views from the layout (item_song.xml)
            songName = itemView.findViewById(R.id.song_name);
            artistName = itemView.findViewById(R.id.artist_name);
        }
    }
}
