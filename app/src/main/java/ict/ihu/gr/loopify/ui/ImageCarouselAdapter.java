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


     // Constructor to initialize the adapter with the images array.
     // imageResources Array of resource IDs for the images to be displayed in the carousel.
    public ImageCarouselAdapter(int[] imageResources) {
        this.imageResources = imageResources;  // Assign the passed array to the adapter
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each image item (item_image.xml)
        // This defines how each image is presented in the carousel
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);

        // Return a new ViewHolder that contains the inflated item layout
        return new ImageViewHolder(view);
    }


     // Called by RecyclerView to display the data at the specified position.
     // This method sets the appropriate image to the ImageView for each position in the carousel.

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // holder holds the view for the current item.
        // position is the position of the current item in the data set (imageResources[]).

        // Set the image resource for the current position in the carousel
        // imageResources[position] fetches the appropriate image from the array
        holder.imageView.setImageResource(imageResources[position]);
    }


    // Returns the total number of images
    @Override
    public int getItemCount() {
        return imageResources.length;  // Return the number of images in the carousel
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        // ImageView that will display each image in the carousel
        public ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find the ImageView in the item layout (item_image.xml) using its ID
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
