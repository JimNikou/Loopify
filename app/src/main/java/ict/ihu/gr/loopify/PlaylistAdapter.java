// PlaylistAdapter.java
package ict.ihu.gr.loopify.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ict.ihu.gr.loopify.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final String[] songNames;
    private final String[] artistNames;

    public PlaylistAdapter(String[] songNames, String[] artistNames) {
        this.songNames = songNames;
        this.artistNames = artistNames;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_song, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.songNameTextView.setText(songNames[position]);
        holder.artistNameTextView.setText(artistNames[position]);
    }

    @Override
    public int getItemCount() {
        return songNames.length;
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        public final TextView songNameTextView;
        public final TextView artistNameTextView;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.song_name);
            artistNameTextView = itemView.findViewById(R.id.artist_name);
        }
    }
}
