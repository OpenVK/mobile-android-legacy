package uk.openvk.android.legacy;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.PluralsRes;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.api.entities.OvkExpandableText;
import uk.openvk.android.legacy.api.entities.OvkLink;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.AuthActivity;
import uk.openvk.android.legacy.ui.core.activities.DebugMenuActivity;
import uk.openvk.android.legacy.ui.core.activities.MainActivity;
import uk.openvk.android.legacy.ui.core.activities.NewPostActivity;
import uk.openvk.android.legacy.ui.core.activities.WallPostActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.ui.core.fragments.app.NewsfeedFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.legacy.ui.list.adapters.SlidingMenuAdapter;
import uk.openvk.android.legacy.ui.list.items.InstanceAccount;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuItem;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;

/** Global.java - global methods for application
 *
 *  OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class Global {

    private Context ctx;

    public Global() {

    }

    public Global(Context ctx) {
        this.ctx = ctx;
    }

    public static int scale(float dip) {
        return Math.round(dip);
    }

    public boolean isTablet() {
        if(ctx != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                DisplayMetrics dismetrics = ctx.getResources().getDisplayMetrics();
                float dpWidth = dismetrics.widthPixels / dismetrics.density;
                if (dpWidth >= 600) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public long getHeapSize() {
        Runtime rt = Runtime.getRuntime();
        return rt.maxMemory();
    }

    public static float getSmalledWidth(WindowManager wm) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        int widthPixels = 0;
        int heightPixels = 0;
        if(Build.VERSION.SDK_INT >= 14) {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                widthPixels = (Integer) mGetRawW.invoke(display);
                heightPixels = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                widthPixels = display.getWidth();
                heightPixels= display.getHeight();
            }
        } else {
            widthPixels = 0;
            heightPixels = 0;
        }
        float scaleFactor = metrics.density;
        float widthDp = 0;
        float heightDp = 0;
        if(scaleFactor > 0) {
            widthDp = (float) widthPixels / scaleFactor;
            heightDp = (float) heightPixels / scaleFactor;
        } else {
            widthDp = (float) widthPixels;
            heightDp = (float) heightPixels;
        }
        return Math.min(widthDp, heightDp);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String GetSHA256Hash(String text) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes());
        byte[] digest = md.digest();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
            return Base64.encodeToString(digest, Base64.DEFAULT);
        } else {
            try {
                return bytesToHex(digest);
            } catch(Exception ex) {
                return "";
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned formatLinksAsHtml(String original_text) {
        String[] lines = original_text.split("\r\n|\r|\n");
        StringBuilder text_llines = new StringBuilder();
        Pattern pattern = Pattern.compile("\\[(.+?)\\]|((http|https)://)(www.)?[a-zA-Z0-9@:%._" +
                "\\+~#?&//=]{1,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
        Matcher matcher = pattern.matcher(original_text);
        boolean regexp_search = matcher.find();
        String text = original_text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
        text = text.replace("\r\n", "<br>").replace("\n", "<br>");
        int regexp_results = 0;
        while(regexp_search) {
            String block = matcher.group();
            if(block.startsWith("[") && block.endsWith("]")) {
                OvkLink link = new OvkLink();
                String[] markup = block.replace("[", "").replace("]", "")
                        .replace("<", "").replace("\"", "").replace(">", "")
                        .split("\\|");
                link.screen_name = markup[0];
                if (markup.length == 2) {
                    if (markup[0].startsWith("id")) {
                        link.url = String.format("openvk://profile/%s", markup[0]);
                        link.name = markup[1];
                    } else if (markup[0].startsWith("club")) {
                        link.url = String.format("openvk://group/%s", markup[0]);
                        link.name = markup[1];
                    }
                    link.name = markup[1];
                    if (markup[0].startsWith("id") || markup[0].startsWith("club")) {
                        text = text.replace(block, String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                    }
                }
            } else if(block.startsWith("https://") || block.startsWith("http://")) {
                text = text.replace(block, String.format("<a href=\"%s\">%s</a>", block, block));
            }
            regexp_results = regexp_results + 1;
            regexp_search = matcher.find();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(text);
        }
    }

    @SuppressWarnings("deprecation")
    public static OvkExpandableText formatLinksAsHtml(String original_text, int end_number) {
        String[] lines = original_text.split("\r\n|\r|\n");
        StringBuilder text_llines = new StringBuilder();
        Pattern pattern = Pattern.compile("\\[(.+?)\\]|((http|https)://)(www.)?[a-zA-Z0-9@:%._" +
                "\\+~#?&//=]{1,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
        Matcher matcher = pattern.matcher(original_text);
        boolean regexp_search = matcher.find();
        String text = original_text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
        text = text.replace("\r\n", "<br>").replace("\n", "<br>");
        int regexp_results = 0;
        while(regexp_search) {
            if(matcher.start() < end_number) {
                String block = matcher.group();
                if (block.startsWith("[") && block.endsWith("]")) {
                    OvkLink link = new OvkLink();
                    String[] markup = block.replace("[", "").replace("]", "")
                            .replace("<", "").replace(">", "").replace("\"", "")
                            .split("\\|");
                    link.screen_name = markup[0];
                    if (markup.length == 2) {
                        if (markup[0].startsWith("id")) {
                            link.url = String.format("openvk://profile/%s", markup[0]);
                            link.name = markup[1];
                        } else if (markup[0].startsWith("club")) {
                            link.url = String.format("openvk://group/%s", markup[0]);
                            link.name = markup[1];
                        }
                        link.name = markup[1];
                        if (markup[0].startsWith("id") || markup[0].startsWith("club")) {
                            text = text.replace(block, String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                        }
                    }
                } else if (block.startsWith("https://") || block.startsWith("http://")) {
                    text = text.replace(block, String.format("<a href=\"%s\">%s</a>", block, block));
                }
            }
            regexp_results = regexp_results + 1;
            regexp_search = matcher.find();
        }

        Spanned html;
        if(text.length() >= end_number) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                html = Html.fromHtml(text.substring(0, end_number - 1) + "...", Html.FROM_HTML_MODE_COMPACT);
            } else {
                html = Html.fromHtml(text.substring(0, end_number - 1) + "...");
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                html = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
            } else {
                html = Html.fromHtml(text);
            }
        }
        return new OvkExpandableText(html, text.length(), end_number);
    }

    public static void fixWindowPadding(View view, Resources.Theme theme) {
        // Fixes cropped content in Android API 28 (9)+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final TypedArray styledAttributes = theme.obtainStyledAttributes(
                    new int[] { android.R.attr.actionBarSize });
            int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
            ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin = 0;
        }
    }

    public static void fixWindowPadding(Window window, Resources.Theme theme) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View view = window.getDecorView();
            int actionBarId = view.getContext().getResources().getIdentifier("action_bar", "id", "android");
            ViewGroup actionBarView = (ViewGroup) view.findViewById(actionBarId);
            Resources res = view.getContext().getResources();
            try {
                if(view.getContext().getApplicationContext() instanceof OvkApplication) {
                    Field f = actionBarView.getClass().getSuperclass().getDeclaredField("mContentHeight");
                    f.setAccessible(true);
                    OvkApplication app = ((OvkApplication) view.getContext().getApplicationContext());
                    if(!app.isTablet) {
                        if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            f.set(actionBarView, (int)res.getDimension(R.dimen.actionbar_size));
                        } else {
                            f.set(actionBarView, (int)res.getDimension(R.dimen.landscape_actionbar_size));
                        }
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getPluralQuantityString(Context ctx, @PluralsRes int id, int value) {
        String qStr = "";
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            qStr = ctx.getResources().getQuantityString(id, value, value);
        } else {
            // Using patched getQuantityString() method version for Android 2.x
            if(ctx instanceof OvkApplication) {
                OvkApplication app = ((OvkApplication) ctx);
                qStr = app.pluralResources.getQuantityString(id, value, value);
            }
        }
        return qStr;
    }

    // Workaround for Plurals methods, because they only work with int (32-bit) numbers.
    public static int getEndNumberFromLong(long number) {
        int end_number = 0;
        if(Long.toString(number).endsWith("1")) {
            end_number = 1;
        } else if(Long.toString(number).endsWith("2")) {
            end_number = 2;
        } else if(Long.toString(number).endsWith("3")) {
            end_number = 3;
        } else if(Long.toString(number).endsWith("4")) {
            end_number = 4;
        } else if(Long.toString(number).endsWith("5")) {
            end_number = 5;
        } else if(Long.toString(number).endsWith("6")) {
            end_number = 6;
        } else if(Long.toString(number).endsWith("7")) {
            end_number = 7;
        } else if(Long.toString(number).endsWith("8")) {
            end_number = 8;
        } else if(Long.toString(number).endsWith("9")) {
            end_number = 9;
        }
        return end_number;
    }

    public static void loadAccounts(Context ctx, ArrayList<InstanceAccount> accountArray,
                                    AccountManager accountManager, SharedPreferences instance_prefs) {
        android.accounts.Account[] accounts = accountManager.getAccounts();
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        long current_uid = global_prefs.getLong("current_uid", 0);
        String current_instance = global_prefs.getString("current_instance", "");
        String package_name = ctx.getApplicationContext().getPackageName();
        @SuppressLint("SdCardPath") String profile_path =
                String.format("/data/data/%s/shared_prefs", package_name);
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        String file_extension;
        String account_names[] = new String[0];
        Context app_ctx = ctx.getApplicationContext();
        accountArray.clear();
        try {
            for (File prefs_file : prefs_files) {
                String filename = prefs_file.getName();
                if (prefs_file.getName().startsWith("instance")
                        && prefs_file.getName().endsWith(".xml")) {
                    SharedPreferences prefs =
                            ctx.getSharedPreferences(
                                    filename.substring(0, filename.length() - 4), 0);
                    String name = prefs.getString("account_name", "");
                    long uid = prefs.getLong("uid", 0);
                    String server = prefs.getString("server", "");
                    if (server.length() > 0 && uid > 0 && name.length() > 0) {
                        InstanceAccount account = new InstanceAccount(name, uid, server);
                        try {
                            accountArray.add(account);
                        } catch (ArrayIndexOutOfBoundsException ignored) {

                        }
                    }
                }
            }
            account_names = new String[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                if (accounts[i] != null &&
                        accounts[i].name.equals(instance_prefs.getString("account_name", "")) &&
                        accounts[i].type.equals("uk.openvk.android.legacy.account")) {
                    account_names[i] = accounts[i].name;
                    if (ctx instanceof AppActivity) {
                        AppActivity app_a = ((AppActivity) ctx);
                        app_a.androidAccount = accounts[i];
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void setSlidingMenu(Context ctx, View menuLayout, SlidingMenu menu) {
        menu.setMode(SlidingMenu.LEFT);
        menu.setBehindWidth((int) (ctx.getResources().getDisplayMetrics().density * 260));
        menu.setMenu(menuLayout);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setFadeDegree(0.8f);
        menu.attachToActivity(((Activity) ctx), SlidingMenu.SLIDING_WINDOW);
        menu.setSlidingEnabled(true);
    }

    public static ArrayList<SlidingMenuItem> createSlidingMenuItems(Context ctx) {
        ArrayList<SlidingMenuItem> slidingMenuArray = new ArrayList<SlidingMenuItem>();
        for (int slider_menu_item_index = 0;
             slider_menu_item_index < ctx.getResources().getStringArray(R.array.leftmenu).length;
             slider_menu_item_index++) {
            if (slider_menu_item_index == 0) {
                slidingMenuArray.add(new SlidingMenuItem(
                        ctx.getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                        0, ctx.getResources().getDrawable(R.drawable.ic_left_friends)));
            } else if (slider_menu_item_index == 1) {
                //slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(
                // R.array.leftmenu)[slider_menu_item_index], 0,
                // getResources().getDrawable(R.drawable.ic_left_photos)));
            } else if (slider_menu_item_index == 2) {
                //slidingMenuArray.add(new SlidingMenuItem(
                // getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                // 0, getResources().getDrawable(R.drawable.ic_left_video)));
            } else if (slider_menu_item_index == 3) {
                slidingMenuArray.add(new SlidingMenuItem(
                        ctx.getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                        0, ctx.getResources().getDrawable(R.drawable.ic_left_messages)));
            } else if (slider_menu_item_index == 4) {
                slidingMenuArray.add(new SlidingMenuItem(
                        ctx.getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                        0, ctx.getResources().getDrawable(R.drawable.ic_left_groups)));
            } else if (slider_menu_item_index == 5) {
                slidingMenuArray.add(new SlidingMenuItem(
                        ctx.getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                        0, ctx.getResources().getDrawable(R.drawable.ic_left_notes)));
            } else if (slider_menu_item_index == 6) {
                slidingMenuArray.add(new SlidingMenuItem(
                        ctx.getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                        0, ctx.getResources().getDrawable(R.drawable.ic_left_news)));
            } else if (slider_menu_item_index == 7) {
                    /* Not implemented!
                    /
                    /  slidingMenuArray.add(new SlidingMenuItem(
                    /  getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                    /  0, getResources().getDrawable(R.drawable.ic_left_feedback)));
                    */
            } else if (slider_menu_item_index == 8) {
                    /* Not implemented!
                    /
                    /  slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(
                    /  R.array.leftmenu)[slider_menu_item_index],
                    /  0, getResources().getDrawable(R.drawable.ic_left_fave)));
                    */
            } else if (slider_menu_item_index == 9) {
                slidingMenuArray.add(new SlidingMenuItem(
                        ctx.getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index],
                        0, ctx.getResources().getDrawable(R.drawable.ic_left_settings)));
            }
        }
        return slidingMenuArray;
    }

    public static ArrayList<SlidingMenuItem> createAccountSlidingMenuItems(Context ctx) {
        ArrayList<SlidingMenuItem> slidingMenuArray = new ArrayList<SlidingMenuItem>();
        for (int slider_menu_item_index = 0;
             slider_menu_item_index < ctx.getResources().getStringArray(R.array.leftmenu_account).length;
             slider_menu_item_index++) {
                slidingMenuArray.add(new SlidingMenuItem(
                    ctx.getResources().getStringArray(R.array.leftmenu_account)[slider_menu_item_index]));
        }
        return slidingMenuArray;
    }

    public void openChangeAccountDialog(final Context ctx, SharedPreferences global_prefs) {
        int valuePos = 0;
        final ArrayList<InstanceAccount> accountArray = new ArrayList<>();
        final int[] selectedPosition = {0};
        if(global_prefs == null) {
            global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        long current_uid = global_prefs.getLong("current_uid", 0);
        String current_instance = global_prefs.getString("current_instance", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        String package_name = ctx.getApplicationContext().getPackageName();
        @SuppressLint("SdCardPath") String profile_path =
                String.format("/data/data/%s/shared_prefs", package_name);
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        String file_extension;
        String account_names[] = new String[0];
        Context app_ctx = ctx.getApplicationContext();
        accountArray.clear();
        try {
            for (File prefs_file : prefs_files) {
                String filename = prefs_file.getName();
                if (prefs_file.getName().startsWith("instance")
                        && prefs_file.getName().endsWith(".xml")) {
                    SharedPreferences prefs =
                            ctx.getSharedPreferences(
                                    filename.substring(0, filename.length() - 4), 0);
                    String name = prefs.getString("account_name", "[Unknown account]");
                    long uid = prefs.getLong("uid", 0);
                    String server = prefs.getString("server", "");
                    if(server.length() > 0 && uid > 0 && name.length() > 0) {
                        InstanceAccount account = new InstanceAccount(name, uid, server);
                        accountArray.add(account);
                    }
                }
            }
            account_names = new String[accountArray.size()];
            for(int i = 0; i < accountArray.size(); i++) {
                account_names[i] = accountArray.get(i).name;
                if (accountArray.get(i).instance.equals(current_instance)) {
                    valuePos = i;
                }
            }
            Log.d(OvkApplication.APP_TAG, String.format("Files: %s", account_names.length));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        builder.setSingleChoiceItems(account_names, valuePos,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPosition[0] = which;
                    }
                }
        );
        OvkAlertDialog dialog = new OvkAlertDialog(ctx);
        dialog.build(builder, ctx.getResources().getString(R.string.sett_account), "", null, "listDlg");
        final SharedPreferences finalGlobal_prefs = global_prefs;
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, ctx.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = finalGlobal_prefs.edit();
                        editor.putString("current_instance", accountArray.get(selectedPosition[0]).instance);
                        editor.putLong("current_uid", accountArray.get(selectedPosition[0]).id);
                        editor.commit();
                        dialog.dismiss();
                        if(ctx instanceof Activity) {
                            ((Activity) ctx).finish();
                            Intent intent = new Intent(ctx, AppActivity.class);
                            ctx.startActivity(intent);
                            System.exit(0);
                        } else {
                            Toast.makeText(ctx, R.string.sett_app_restart_required,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                ctx.getResources().getString(R.string.add),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ctx, AuthActivity.class);
                        intent.putExtra("authFromAppActivity", true);
                        ctx.startActivity(intent);
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                ctx.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public void openChangeAccountDialog(final Context ctx, SharedPreferences global_prefs,
                                        boolean cancelable) {
        int valuePos = 0;
        final ArrayList<InstanceAccount> accountArray = new ArrayList<>();
        final int[] selectedPosition = {0};
        if(global_prefs == null) {
            global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        long current_uid = global_prefs.getLong("current_uid", 0);
        String current_instance = global_prefs.getString("current_instance", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        String package_name = ctx.getApplicationContext().getPackageName();
        @SuppressLint("SdCardPath") String profile_path =
                String.format("/data/data/%s/shared_prefs", package_name);
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        String file_extension;
        String account_names[] = new String[0];
        Context app_ctx = ctx.getApplicationContext();
        accountArray.clear();
        try {
            for (File prefs_file : prefs_files) {
                String filename = prefs_file.getName();
                if (prefs_file.getName().startsWith("instance")
                        && prefs_file.getName().endsWith(".xml")) {
                    SharedPreferences prefs =
                            ctx.getSharedPreferences(
                                    filename.substring(0, filename.length() - 4), 0);
                    String name = prefs.getString("account_name", "[Unknown account]");
                    long uid = prefs.getLong("uid", 0);
                    String server = prefs.getString("server", "");
                    if(server.length() > 0 && uid > 0 && name.length() > 0) {
                        InstanceAccount account = new InstanceAccount(name, uid, server);
                        accountArray.add(account);
                    }
                }
            }
            account_names = new String[accountArray.size()];
            for(int i = 0; i < accountArray.size(); i++) {
                account_names[i] = accountArray.get(i).name;
                if (accountArray.get(i).instance.equals(current_instance)) {
                    valuePos = i;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(accountArray.size() > 0) {
            builder.setSingleChoiceItems(account_names, valuePos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedPosition[0] = which;
                        }
                    }
            );
            OvkAlertDialog dialog = new OvkAlertDialog(ctx);
            dialog.build(builder, ctx.getResources().getString(R.string.sett_account), "", null, "listDlg");
            final SharedPreferences finalGlobal_prefs = global_prefs;
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, ctx.getResources().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = finalGlobal_prefs.edit();
                            editor.putString("current_instance", accountArray.get(selectedPosition[0]).instance);
                            editor.putLong("current_uid", accountArray.get(selectedPosition[0]).id);
                            editor.commit();
                            dialog.dismiss();
                            if (ctx instanceof Activity) {
                                ((Activity) ctx).finish();
                                Intent intent = new Intent(ctx, AppActivity.class);
                                ctx.startActivity(intent);
                                System.exit(0);
                            } else {
                                Toast.makeText(ctx, R.string.sett_app_restart_required,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                    ctx.getResources().getString(R.string.add),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ctx, AuthActivity.class);
                            intent.putExtra("authFromAppActivity", true);
                            ctx.startActivity(intent);
                        }
                    });
            dialog.setCancelable(cancelable);
            dialog.show();
        } else {
            Intent intent = new Intent(ctx, AuthActivity.class);
            System.exit(0);
            ctx.startActivity(intent);
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            long l = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String getUrlArguments(String path) {
        String args = "";
        if (path.startsWith("openvk://profile/")) {
            args = path.substring("openvk://profile/".length());
        } else if (path.startsWith("openvk://group/")) {
            args = path.substring("openvk://group/".length());
        } else if (path.startsWith("openvk://friends/")) {
            args = path.substring("openvk://friends/".length());
        } else if (path.startsWith("openvk://notes/")) {
            args = path.substring("openvk://notes/".length());
        } else if(path.startsWith("https://openvk.su/")) {
            args = path.substring("https://openvk.su/".length());
        } else if(path.startsWith("https://openvk.uk/")) {
            args = path.substring("https://openvk.uk/".length());
        } else if(path.startsWith("http://openvk.xyz/")) {
            args = path.substring("http://openvk.xyz/".length());
        }
        return args;
    }

    public static String formatFileSize(Resources res, long size, String f) {
        if(size >= 1073741824L) {
            return String.format(
                    f,
                    (double)size / (double)1024 / (double)1024 / (double)1024,
                    res.getString(R.string.fsize_gb)
            );
        } else if(size >= 1048576L) {
            return String.format(
                    f,
                    (double)size / (double)1024 / (double)1024,
                    res.getString(R.string.fsize_mb)
            );
        } else if(size >= 1024L) {
            return String.format(
                    f,
                    (double)size / (double)1024,
                    res.getString(R.string.fsize_kb)
            );
        } else {
            return String.format("%s %s",
                    size, res.getString(R.string.fsize_b)
            );
        }
    }

    public static void allowPermissionDialog(final Context ctx, boolean readPerm) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getResources().getString(R.string.allow_permisssion_in_storage_title));
        if(readPerm) {
            builder.setMessage(ctx.getResources().getString(R.string.allow_read_permisssion_in_storage));
        } else {
            builder.setMessage(ctx.getResources().getString(R.string.allow_write_permisssion_in_storage));
        }
        builder.setPositiveButton(ctx.getResources().getString(R.string.open_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", ctx.getPackageName(), null);
                intent.setData(uri);
                ctx.startActivity(intent);
            }
        });
        builder.setNegativeButton(ctx.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public static void openRepostDialog(Context ctx, final OpenVKAPI ovk_api, String where, final WallPost post) {
        if(where.equals("own_wall")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            final View repost_view = ((Activity)ctx).getLayoutInflater().inflate(R.layout.dialog_repost_msg,
                    null, false);
            final EditText text_edit = ((EditText) repost_view.findViewById(R.id.text_edit));
            builder.setView(repost_view);
            builder.setPositiveButton(R.string.ok, null);
            builder.setNegativeButton(R.string.cancel, null);
            final OvkAlertDialog dialog = new OvkAlertDialog(ctx);
            dialog.build(builder, ctx.getResources().getString(R.string.repost_dlg_title), "", repost_view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final Button ok_btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        if(ok_btn != null) {
                            ok_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        String msg_text = ((EditText)
                                                repost_view.findViewById(R.id.text_edit)).getText()
                                                .toString();
                                        ovk_api.wall.repost(ovk_api.wrapper, post.owner_id, post.post_id, msg_text);
                                        dialog.close();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
            }
            dialog.show();
        }
    }

    public static void openWallRepostComments(Context ctx, int position, View view) {
        WallPost item;
        Intent intent = new Intent(ctx.getApplicationContext(), WallPostActivity.class);
        SharedPreferences global_prefs = null;
        OpenVKAPI ovk_api = null;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if(ctx instanceof AppActivity) {
            ovk_api = ((AppActivity) ctx).ovk_api;
        } else if(ctx instanceof ProfileIntentActivity) {
            ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
        } else if(ctx instanceof GroupIntentActivity) {
            ovk_api = ((GroupIntentActivity) ctx).ovk_api;
        } else {
            return;
        }
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = ovk_api.wall.getWallItems().get(position);
            intent.putExtra("where", "wall");
        } else {
            item = ovk_api.newsfeed.getWallPosts().get(position);
            intent.putExtra("where", "newsfeed");
        }
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("post_id", item.repost.newsfeed_item.post_id);
            intent.putExtra("owner_id", item.repost.newsfeed_item.owner_id);
            intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                    ovk_api.account.last_name));
            intent.putExtra("account_id", ovk_api.account.id);
            intent.putExtra("post_author_id", item.repost.newsfeed_item.author_id);
            intent.putExtra("post_author_name", item.repost.newsfeed_item.name);
            intent.putExtra("post_json", item.repost.newsfeed_item.getJSONString());
            ctx.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addToFriends(OpenVKAPI ovk_api, long user_id) {
        if(user_id != ovk_api.account.id) {
            ovk_api.friends.add(ovk_api.wrapper, user_id);
        }
    }

    public static void deleteFromFriends(OpenVKAPI ovk_api, long user_id) {
        if(user_id != ovk_api.account.id) {
            ovk_api.friends.delete(ovk_api.wrapper, user_id);
        }
    }

    public static void showAuthorPage(Context ctx, String where, int position) {
        WallPost item;
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        OpenVKAPI ovk_api = null;
        if(ctx instanceof AppActivity) {
            ovk_api = ((AppActivity) ctx).ovk_api;
        } else if(ctx instanceof ProfileIntentActivity) {
            ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
        } else if(ctx instanceof GroupIntentActivity) {
            ovk_api = ((GroupIntentActivity) ctx).ovk_api;
        } else {
            return;
        }

        if (where.equals("profile")) {
            item = ovk_api.wall.getWallItems().get(position);
        } else {
            item = ovk_api.newsfeed.getWallPosts().get(position);
        }

        if(item.author_id != ovk_api.account.id) {
            String url = "";
            if (item.author_id < 0) {
                url = "openvk://group/" + "club" + -item.author_id;
            } else {
                url = "openvk://profile/" + "id" + item.author_id;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setPackage("uk.openvk.android.legacy");
            i.setData(Uri.parse(url));
            ctx.startActivity(i);
        } else {
            if(ctx instanceof AppActivity) {
                ((AppActivity) ctx).openAccountProfile();
            }
        }
    }

    public static void addLike(Context ctx, int position, String post, View view) {
        WallPost item;
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        OpenVKAPI ovk_api = null;
        NewsfeedFragment newsfeedFragment = null;
        WallLayout wallLayout = null;
        if(ctx instanceof AppActivity) {
            ovk_api = ((AppActivity) ctx).ovk_api;
            newsfeedFragment = ((AppActivity) ctx).newsfeedFragment;
        } else if(ctx instanceof ProfileIntentActivity) {
            ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            ProfileFragment profileFragment = ((ProfileIntentActivity) ctx).profileFragment;
            if(profileFragment.getView() != null) {
                wallLayout = (profileFragment.getView().findViewById(R.id.wall_layout));
            } else {
                return;
            }
        } else if(ctx instanceof GroupIntentActivity) {
            ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            wallLayout = ((GroupIntentActivity) ctx).findViewById(R.id.wall_layout);
        } else {
            return;
        }
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = ovk_api.wall.getWallItems().get(position);
            if(wallLayout != null) {
                wallLayout.select(position, "likes", "add");
            } else {
                return;
            }
        } else {
            item = ovk_api.newsfeed.getWallPosts().get(position);
            if(newsfeedFragment != null) {
                newsfeedFragment.select(position, "likes", "add");
            } else {
                return;
            }
        }
        ovk_api.likes.add(ovk_api.wrapper, item.owner_id, item.post_id, position);
    }

    public static void deleteLike(Context ctx, int position, String post, View view) {
        WallPost item;
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        OpenVKAPI ovk_api = null;
        NewsfeedFragment newsfeedFragment = null;
        WallLayout wallLayout = null;
        if(ctx instanceof AppActivity) {
            ovk_api = ((AppActivity) ctx).ovk_api;
            newsfeedFragment = ((AppActivity) ctx).newsfeedFragment;
        } else if(ctx instanceof ProfileIntentActivity) {
            ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            ProfileFragment profileFragment = ((ProfileIntentActivity) ctx).profileFragment;
            if(profileFragment.getView() != null) {
                wallLayout = (profileFragment.getView().findViewById(R.id.wall_layout));
            } else {
                return;
            }
        } else if(ctx instanceof GroupIntentActivity) {
            ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            wallLayout = ((GroupIntentActivity) ctx).findViewById(R.id.wall_layout);
        } else {
            return;
        }
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = ovk_api.wall.getWallItems().get(position);
            if(wallLayout != null) {
                wallLayout.select(0, "likes", "delete");
            } else {
                return;
            }
        } else {
            item = ovk_api.newsfeed.getWallPosts().get(position);
            if(newsfeedFragment != null) {
                newsfeedFragment.select(0, "likes", "delete");
            } else {
                return;
            }
        }
        ovk_api.likes.delete(ovk_api.wrapper, item.owner_id, item.post_id, position);
    }

    public static void loadMoreWallPosts(OpenVKAPI ovk_api) {
        if(ovk_api.wall != null) {
            ovk_api.wall.get(ovk_api.wrapper, ovk_api.account.id, 25, ovk_api.wall.next_from);
        }
    }

    public static void loadMoreFriends(OpenVKAPI ovk_api) {
        if(ovk_api.friends != null) {
            ovk_api.friends.get(ovk_api.wrapper, ovk_api.account.id, 25, ovk_api.friends.offset);
        }
    }

    public static void loadMoreGroups(OpenVKAPI ovk_api) {
        if(ovk_api.groups != null) {
            ovk_api.groups.getGroups(ovk_api.wrapper, ovk_api.account.id, 25, ovk_api.groups.getList().size());
        }
    }

    public static void openIntentFromCounters(Context ctx, String action) {
        if(!BuildConfig.BUILD_TYPE.equals("release"))
            Log.d(OvkApplication.APP_TAG, "Opening intent from " + action);
        if(action.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setPackage("uk.openvk.android.legacy");
            i.setData(Uri.parse(action));
            ctx.startActivity(i);
        }
    }

    public static void openWallComments(Context ctx, int position, View view) {
        OpenVKAPI ovk_api = null;
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if(ctx instanceof AppActivity) {
            ovk_api = ((AppActivity) ctx).ovk_api;
        } else if(ctx instanceof ProfileIntentActivity) {
            ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
        } else if(ctx instanceof GroupIntentActivity) {
            ovk_api = ((GroupIntentActivity) ctx).ovk_api;
        } else {
            return;
        }
        if(ovk_api.account != null) {
            WallPost item;
            Intent intent = new Intent(ctx.getApplicationContext(), WallPostActivity.class);
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
                intent.putExtra("where", "wall");
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
                intent.putExtra("where", "newsfeed");
            }
            try {
                intent.putExtra("post_id", item.post_id);
                intent.putExtra("owner_id", item.owner_id);
                intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                        ovk_api.account.last_name));
                intent.putExtra("account_id", ovk_api.account.id);
                intent.putExtra("post_author_id", item.author_id);
                intent.putExtra("post_author_name", item.name);
                intent.putExtra("post_json", item.getJSONString());
                ctx.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void openNewPostActivity(Context ctx, OpenVKAPI ovk_api) {
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        try {
            Intent intent = new Intent(ctx.getApplicationContext(), NewPostActivity.class);
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                intent.putExtra("owner_id", ovk_api.user.id);
            } else {
                intent.putExtra("owner_id", ovk_api.account.id);
            }
            intent.putExtra("account_id", ovk_api.account.id);
            intent.putExtra("account_first_name", ovk_api.account.user.first_name);
            ctx.startActivity(intent);
        } catch (Exception ignored) {

        }
    }
}
