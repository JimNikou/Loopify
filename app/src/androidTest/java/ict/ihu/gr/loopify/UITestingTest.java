package ict.ihu.gr.loopify;

import ict.ihu.gr.loopify.SimpleIdlingResource;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.Gravity;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.Espresso;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import android.content.Intent;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.internal.bind.TreeTypeAdapter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Type;

@RunWith(AndroidJUnit4.class)
public class UITestingTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Test
    public void SignUpAfterMainActivityIsCalled() {
        onView(withId(R.id.emailField)).perform(typeText("zedkairengar@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordField)).perform(typeText("this_is_a_test_password123"), closeSoftKeyboard());
        onView(withId(R.id.signupButton))
                .check(matches(isDisplayed()))  // Check that the button is visible
                .perform(click());  // Then perform the click action

    }

    @Test
    public void LogInAfterMainActivityIsCalled() {
        onView(withId(R.id.emailField)).perform(typeText("zedkairengar@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordField)).perform(typeText("this_is_a_test_password123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()))  // Check that the button is visible
                .perform(click());  // Then perform the click action

    }

    @Test
    public void SignUpWithEmptyEmail() {
        onView(withId(R.id.passwordField)).perform(typeText("this_is_a_test_password123"), closeSoftKeyboard());
        onView(withId(R.id.signupButton))
                .check(matches(isDisplayed()))  // Check that the button is visible
                .perform(click());  // Then perform the click action

        onView(withId(R.id.errorMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.errorMessage)).check(matches(withText("Please enter email and password")));
    }

    @Test
    public void SignUpWithEmptyPassword() {
        onView(withId(R.id.emailField)).perform(typeText("zedkairengar@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.signupButton))
                .check(matches(isDisplayed()))  // Check that the button is visible
                .perform(click());  // Then perform the click action

        onView(withId(R.id.errorMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.errorMessage)).check(matches(withText("Please enter email and password")));
    }

    @Test
    public void NavigateToSearchFragment() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(createMainActivityIntent(true));
        onView(withId(R.id.nav_search)).perform(click());
        onView(withId(R.id.searchBar)).check(matches(isDisplayed()));
    }

//    @Test
//    public void NavigateToYourLibraryFragment() {
//        //crahsarei dioti den exei mock object gia na exei uid apo to firebase.
//        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(createMainActivityIntent(true));
//        onView(withId(R.id.nav_library)).perform(click());
//        onView(withId(R.layout.fragment_your_library)).check(matches(isDisplayed()));
//    }


    @Test
    public void SearchSongInSearchFragment() {
        //bypassing the login screen
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(createMainActivityIntent(true));

        onView(withId(R.id.nav_search)).perform(click());
        onView(withId(R.id.searchBar)).check(matches(isDisplayed()));
        onView(withId(R.id.searchBar)).perform(typeText("Michael Jackson"));
        onView(withId(R.id.searchBar)).perform(click());
    }


    @Test
    public void OpenDrawerAndGoIntoAboutPage(){
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(createMainActivityIntent(true));
        onView(withId(R.id.app_bar_main)).perform(click());

    }



    @Test
    public void OpenHipHopStaticPlaylistAndViewAnArtistsBio(){
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(createMainActivityIntent(true));

        //dependency conflicts
//        onView(withId(R.id.recycler_view))
//                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }



    private Intent createMainActivityIntent(boolean isTestMode) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("TEST_MODE", isTestMode);
        return intent;
    }
}
