package ict.ihu.gr.loopify.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import ict.ihu.gr.loopify.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final String[] songNames;
    private final String[] artistNames;
    private final String[] imageUrls;  // Change to String[] for image URLs

    public PlaylistAdapter(String[] songNames, String[] artistNames, String[] imageUrls) {
        this.songNames = songNames;
        this.artistNames = artistNames;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.songNameTextView.setText(songNames[position]);
        holder.artistNameTextView.setText(artistNames[position]);

        // Use Glide to load the image from URL into the ImageView
        Glide.with(holder.songImageView.getContext())
                .load(imageUrls[position])  // Load image from URL
                .into(holder.songImageView);  // Set image into ImageView

        // Set up click listener for the song options (⋮)
        holder.songOptionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show options when clicked
                Toast.makeText(v.getContext(), "Options clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return songNames.length;
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        public final TextView songNameTextView;
        public final TextView artistNameTextView;
        public final ImageView songImageView;  // ImageView for song image
        public final TextView songOptionsTextView;  // Add reference to the song options (⋮) TextView

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.song_name);
            artistNameTextView = itemView.findViewById(R.id.artist_name);
            songImageView = itemView.findViewById(R.id.song_image);  // Initialize ImageView correctly
            songOptionsTextView = itemView.findViewById(R.id.song_options);  // Initialize song options TextView
        }
    }
}
