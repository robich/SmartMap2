package ch.epfl.smartmap.util;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

/**
 * A base implementation of {@link SystemUiHider}. Uses APIs available in all API levels to show and hide the
 * status
 * bar.
 */
public class SystemUiHiderBase extends SystemUiHider {
    /**
     * Whether or not the system UI is currently visible. This is a cached value from calls to {@link #hide()}
     * and {@link #show()}.
     */
    private boolean mVisible = true;

    /**
     * Constructor not intended to be called by clients. Use {@link SystemUiHider#getInstance} to obtain an
     * instance.
     */
    protected SystemUiHiderBase(Activity activity, View anchorView, int flags) {
        super(activity, anchorView, flags);
    }

    @Override
    public void hide() {
        if ((getFlags() & FLAG_FULLSCREEN) != 0) {
            super.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        getOnVisibilityChangeListener().onVisibilityChange(false);
        mVisible = false;
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void setUp() {
        if ((getFlags() & FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES) == 0) {
            getActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public void show() {
        if ((getFlags() & FLAG_FULLSCREEN) != 0) {
            getActivity().getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        getOnVisibilityChangeListener().onVisibilityChange(true);
        mVisible = true;
    }
}
