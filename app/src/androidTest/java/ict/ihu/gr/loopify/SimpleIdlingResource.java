package ict.ihu.gr.loopify;

import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.IdlingResource.ResourceCallback;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleIdlingResource implements IdlingResource {

    private static AtomicBoolean isIdleNow = new AtomicBoolean(true);
    private static ResourceCallback resourceCallback;

    @Override
    public String getName() {
        return SimpleIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdleNow.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    // Call this method when the task is finished
    public static void setIdleState(boolean isIdle) {
        isIdleNow.set(isIdle);
        if (isIdle && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }
}
