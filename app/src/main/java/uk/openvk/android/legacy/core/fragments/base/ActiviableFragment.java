package uk.openvk.android.legacy.core.fragments.base;

import android.support.v4.app.Fragment;

public class ActiviableFragment extends Fragment {
    private boolean isActivated;

    public boolean isActivated() {
        return isActivated;
    }

    protected void activate() {
        onActivated();
    }

    public void onActivated() {
        isActivated = true;
    }

    protected void deactivate() {
        onDeactivated();
    }

    public void onDeactivated() {
        isActivated = false;
    }
}
