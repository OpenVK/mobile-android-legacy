package uk.openvk.android.legacy;

import android.graphics.drawable.Drawable;

class SlidingMenuItem {
    String name;
    int counter;
    Drawable icon;

    SlidingMenuItem(String _describe, int _counter, Drawable _icon) {
        name = _describe;
        counter = _counter;
        icon = _icon;
    }
}
