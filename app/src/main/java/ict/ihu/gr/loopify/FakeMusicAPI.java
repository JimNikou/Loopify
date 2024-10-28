package ict.ihu.gr.loopify;  // Adjust this to match your package name

import java.util.Random;

public class FakeMusicAPI {

    // Function that simulates an API request by generating dynamic data
    public static String[] getSongDetails(String songName) {
        Random random = new Random();
        String[] songDetails = new String[3];

        // Generate a random number for artist, album, and genre
        int randomNum = random.nextInt(1001);  // Random number between 0 and 1000

        // Return fake details based on the random number
        songDetails[0] = "Artist " + randomNum;  // Artist name
        songDetails[1] = "Album " + randomNum;   // Album name
        songDetails[2] = "Genre " + randomNum;   // Genre name

        return songDetails;
    }
}
