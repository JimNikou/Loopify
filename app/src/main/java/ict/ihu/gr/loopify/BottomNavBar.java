package ict.ihu.gr.loopify;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import ict.ihu.gr.loopify.databinding.ActivityMainBinding;
import ict.ihu.gr.loopify.ui.home.HomeFragment;

public class BottomNavBar extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);

        // Set default fragment when the activity is first created
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment(); // Load Home Fragment
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment(); // Load Search Fragment
            } else if (itemId == R.id.nav_library) {
                selectedFragment = new YourLibraryFragment(); // Load Library Fragment
            }

            // Only load fragment if one is selected (valid ID)
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }

            return false; // Return false if no valid item is selected
        });
    }

    // Helper function to load the fragment and replace the current one
    private void loadFragment(Fragment fragment) {
        // Begin the transaction to replace the fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, fragment);  // 'nav_host_fragment_content_main' is the container
        transaction.commit(); // Commit the transaction
    }
}
