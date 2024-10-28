package ict.ihu.gr.loopify;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ImageSongMapper {

    // Map image resources to lists of song names
    private static final HashMap<Integer, List<String>> imageSongsMap = new HashMap<>();

    static {
        // For each image resource, add a corresponding playlist of song names (without artists)
        imageSongsMap.put(R.drawable.hiphop_photo, List.of(
                "Hip-Hop Song 1",
                "Hip-Hop Song 2",
                "Hip-Hop Song 3"
        ));

        imageSongsMap.put(R.drawable.house_photo, List.of(
                "House Song 1",
                "House Song 2",
                "House Song 3"
        ));

        imageSongsMap.put(R.drawable.jazz_photo, List.of(
                "Jazz Song 1",
                "Jazz Song 2",
                "Jazz Song 3"
        ));

        imageSongsMap.put(R.drawable.pop_photo, List.of(
                "Pop Song 1",
                "Pop Song 2",
                "Pop Song 3"
        ));

        imageSongsMap.put(R.drawable.rap_photo, List.of(
                "Rap Song 1",
                "Rap Song 2",
                "Rap Song 3"
        ));

        imageSongsMap.put(R.drawable.rock_photo, List.of(
                "Rock Song 1",
                "Rock Song 2",
                "Rock Song 3"
        ));

        imageSongsMap.put(R.drawable.techno_photo, List.of(
                "Techno Song 1",
                "Techno Song 2",
                "Techno Song 3"
        ));
    }

    // Method to get the songs for an image
    public static List<String> getSongsForImage(int imageResource) {
        return imageSongsMap.getOrDefault(imageResource, new ArrayList<>());  // Return an empty list if no songs are found
    }
}
