package ict.ihu.gr.loopify.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ict.ihu.gr.loopify.R;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder> {

    // Array to hold the resource IDs of images (e.g., R.drawable.hiphop_photo)
    private final int[] imageResources;
    private final OnImageClickListener onImageClickListener;

    // Constructor to initialize the adapter with the images array and click listener
    public ImageCarouselAdapter(int[] imageResources, OnImageClickListener onImageClickListener) {
        this.imageResources = imageResources;  // Assign the passed array to the adapter
        this.onImageClickListener = onImageClickListener;  // Assign the passed listener
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each image item (item_image.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view, onImageClickListener, imageResources); // Pass imageResources to ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.imageView.setImageResource(imageResources[position]); // Set image resource
    }

    @Override
    public int getItemCount() {
        return imageResources.length; // Return the number of images in the carousel
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;
        private OnImageClickListener onImageClickListener;
        private final int[] imageResources; // Add an instance variable for imageResources

        public ImageViewHolder(@NonNull View itemView, OnImageClickListener onImageClickListener, int[] imageResources) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            this.onImageClickListener = onImageClickListener;
            this.imageResources = imageResources; // Assign passed imageResources

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Trigger the listener when clicked, passing both the adapter position and the image resource
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(getAdapterPosition(), imageResources[getAdapterPosition()]);
            }
        }
    }

    // Interface for click events
    public interface OnImageClickListener {
        void onImageClick(int position, int imageResource); // Pass both position and image resource
    }
}
