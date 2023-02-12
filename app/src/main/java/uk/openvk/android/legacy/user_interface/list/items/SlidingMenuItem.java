package uk.openvk.android.legacy.user_interface.list.items;

import android.graphics.drawable.Drawable;

public class SlidingMenuItem {
    public String name;
    public int counter;
    public Drawable icon;

    public SlidingMenuItem(String _describe, int _counter, Drawable _icon) {
        name = _describe;
        counter = _counter;
        icon = _icon;
    }
}
