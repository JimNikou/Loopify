package ict.ihu.gr.loopify;

import android.content.Context;
import android.content.Intent;
import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import androidx.core.graphics.ColorUtils;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.ui.settings.SettingsFragment;

public class GeneralSettingsFragment extends Fragment {

    private static final String APP_SETTINGS = "AppSettings";
    private static final String APP_LANGUAGE_KEY = "App_Language";

    public GeneralSettingsFragment() {
        // Required empty public constructor
    }

    public static GeneralSettingsFragment newInstance() {
        return new GeneralSettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general_settings, container, false);

        // Back Button Setup
        Button backButton = view.findViewById(R.id.back_button_general);
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof SettingsFragment) {
                ((SettingsFragment) parentFragment).showNavigationView();
            }
        });

        // Language Selector Button
        Button languageSelectorButton = view.findViewById(R.id.language_selector_button);
        languageSelectorButton.setOnClickListener(v -> showLanguageBottomSheet());

        // Gradient colors for the progress bar
        int[] gradientColors = {
                Color.parseColor("#c144d6"),
                Color.parseColor("#C181D6"),
                Color.parseColor("#FF48FF")
        };

        // SeekBar and TextView Setup
        SeekBar crossfadeSeekBar = view.findViewById(R.id.crossfade_seekbar);
        TextView crossfadeValue = view.findViewById(R.id.crossfade_value);

        // Initialize TextView with 0 seconds (localized)
        crossfadeValue.setText(getString(R.string.seconds_format, 0));

        // Set SeekBar listener
        crossfadeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the TextView with the current SeekBar value (localized)
                crossfadeValue.setText(getString(R.string.seconds_format, progress));

                // Calculate animated color based on progress
                float fraction = (float) progress / seekBar.getMax();
                int animatedColor = interpolateColor(gradientColors, fraction);

                // Apply the color dynamically to the progress bar and thumb
                seekBar.getProgressDrawable().setColorFilter(animatedColor, PorterDuff.Mode.SRC_IN);
                seekBar.getThumb().setColorFilter(animatedColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Add visual feedback by scaling up the thumb
                seekBar.setScaleX(1.2f);
                seekBar.setScaleY(1.2f);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Reset thumb size after interaction ends
                seekBar.setScaleX(1.0f);
                seekBar.setScaleY(1.0f);
            }
        });

        return view;
    }

    /**
     * Helper function to interpolate between colors in the gradient.
     *
     * @param colors   Array of colors to interpolate
     * @param fraction Progress fraction (0 to 1)
     * @return Interpolated color
     */
    private int interpolateColor(int[] colors, float fraction) {
        if (colors.length == 0) return 0;

        // Determine the two colors to interpolate between
        int startIndex = (int) Math.floor(fraction * (colors.length - 1));
        int endIndex = Math.min(startIndex + 1, colors.length - 1);

        float subFraction = (fraction * (colors.length - 1)) - startIndex;

        // Blend the two colors based on the subFraction
        return ColorUtils.blendARGB(colors[startIndex], colors[endIndex], subFraction);
    }


    private void showLanguageBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_language_selector, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetView.findViewById(R.id.language_option_english).setOnClickListener(v -> {
            changeAppLanguage("en");
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.language_option_greek).setOnClickListener(v -> {
            changeAppLanguage("el");
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.language_option_spanish).setOnClickListener(v -> {
            changeAppLanguage("es");
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void changeAppLanguage(String languageCode) {
        // Save the selected language in SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(APP_LANGUAGE_KEY, languageCode);
        editor.apply();

        // Update the app's locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireActivity().getResources().updateConfiguration(config, requireActivity().getResources().getDisplayMetrics());

        // Show a Toast message to inform the user
        Toast.makeText(requireContext(), getString(R.string.language_change_toast), Toast.LENGTH_SHORT).show();

        // Restart the activity using an explicit Intent
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        requireActivity().overridePendingTransition(0, 0); // Optional: Remove activity transition animations
        startActivity(intent);
    }


}
