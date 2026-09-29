/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.list.items;

import android.graphics.drawable.Drawable;

public class SlidingMenuObject {

    public static final int TYPE_MENU_ITEM   = 0x0000;
    public static final int TYPE_CATEGORY    = 0x0001;
    public static final int TYPE_SIMPLE_TEXT = 0x0002;
    public static final int TYPE_PUBLIC_PAGE = 0x0003;

    public String name;
    public int counter;
    public Drawable icon;
    public int type;
    public Object embed;

    public SlidingMenuObject(int type, String name) {
        this.name = name;
        this.type = type;
    }

    public SlidingMenuObject(int type, String name, Object embed) {
        this.name = name;
        this.type = type;
        this.embed = embed;
    }

    public SlidingMenuObject(int type, String name, int counter, Drawable icon) {
        this.name = name;
        this.counter = counter;
        this.icon = icon;
        this.type = type;
    }

    public SlidingMenuObject(String name, int counter, Drawable icon) {
        this.name = name;
        this.counter = counter;
        this.icon = icon;
    }

    public SlidingMenuObject(String name) {
        this.name = name;
    }
}
