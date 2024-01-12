package uk.openvk.android.legacy.core.fragments.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

public class ActiviablePreferenceFragment extends PreferenceFragmentCompatDividers {
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

    public void deactivate() {
        onDeactivated();
    }

    public  void onDeactivated() {
        isActivated = false;
    }

    @Override
    public void onCreatePreferencesFix(Bundle bundle, String s) {

    }
}
