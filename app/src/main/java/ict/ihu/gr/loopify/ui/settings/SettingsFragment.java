package ict.ihu.gr.loopify.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;

import ict.ihu.gr.loopify.AccountSettingsFragment;
import ict.ihu.gr.loopify.AppearanceSettingsFragment;
import ict.ihu.gr.loopify.AudioSettingsFragment;
import ict.ihu.gr.loopify.ContentPreferencesFragment;
import ict.ihu.gr.loopify.GeneralSettingsFragment;
import ict.ihu.gr.loopify.LibrarySettingsFragment;
import ict.ihu.gr.loopify.LogoutSettingsFragment;
import ict.ihu.gr.loopify.NotificationSettingsFragment;
import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.StorageDataSettingsFragment;
import ict.ihu.gr.loopify.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        // Load the saved theme
        SharedPreferences preferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String theme = preferences.getString("app_theme", "default");

        // Set the background based on the theme
        int backgroundResource;
        switch (theme) {
            case "aquamarine":
                backgroundResource = R.drawable.background_aquamarine;
                break;
            case "beige":
                backgroundResource = R.drawable.background_beige;
                break;
            case "gold":
                backgroundResource = R.drawable.background_gold;
                break;
            case "ink":
                backgroundResource = R.drawable.background_ink;
                break;
            default:
                backgroundResource = R.drawable.background;
        }

        // Set the background resource to the view
        binding.getRoot().setBackgroundResource(backgroundResource);

        // Set up the navigation item selection listener directly
        binding.settingsNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_general_settings) {
                    selectedFragment = new GeneralSettingsFragment();
                } else if (item.getItemId() == R.id.nav_audio_settings) {
                    selectedFragment = new AudioSettingsFragment();
                } else if (item.getItemId() == R.id.nav_notification_settings) {
                    selectedFragment = new NotificationSettingsFragment();
                } else if (item.getItemId() == R.id.nav_library_settings) {
                    selectedFragment = new LibrarySettingsFragment();
                } else if (item.getItemId() == R.id.nav_account_settings) {
                    selectedFragment = new AccountSettingsFragment();
                } else if (item.getItemId() == R.id.nav_appearance_settings) {
                    selectedFragment = new AppearanceSettingsFragment();
                } else if (item.getItemId() == R.id.nav_content_preferences_settings) {
                    selectedFragment = new ContentPreferencesFragment();
                } else if (item.getItemId() == R.id.nav_storage_n_data_settings) {
                    selectedFragment = new StorageDataSettingsFragment();
                } else if (item.getItemId() == R.id.nav_logout_settings) {
                    selectedFragment = new LogoutSettingsFragment();
                }

                if (selectedFragment != null) {
                    // Hide the NavigationView to make the subcategory fragment full screen
                    binding.settingsNav.setVisibility(View.GONE);

                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.settings_layout, selectedFragment);
                    transaction.addToBackStack(null);  // Allows back navigation
                    transaction.commit();
                }

                return true;
            }
        });

        return binding.getRoot();
    }

    // Public method to show the NavigationView
    public void showNavigationView() {
        if (binding != null) {
            binding.settingsNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        clearSubcategoryFragments();
    }

    private void clearSubcategoryFragments() {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof GeneralSettingsFragment ||
                    fragment instanceof AudioSettingsFragment ||
                    fragment instanceof NotificationSettingsFragment ||
                    fragment instanceof LibrarySettingsFragment ||
                    fragment instanceof AccountSettingsFragment ||
                    fragment instanceof AppearanceSettingsFragment ||
                    fragment instanceof ContentPreferencesFragment ||
                    fragment instanceof StorageDataSettingsFragment ||
                    fragment instanceof LogoutSettingsFragment) {
                getChildFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
    }
}
