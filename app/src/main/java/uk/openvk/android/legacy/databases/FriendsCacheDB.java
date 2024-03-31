/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.legacy.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.databases.base.CacheDatabase;

public class FriendsCacheDB extends CacheDatabase {

    public static String prefix = "friends";

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String db_name) {
            super(ctx, db_name, null, 1);
        }

        public CacheOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            CacheDatabaseTables.createFriendsTable(database);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
            if (oldVer == 1 && newVer >= oldVer) {
                // TODO: Add database auto-upgrade to new versions
                return;
            }
            onCreate(database);
        }
    }

    public static File getCurrentDatabasePath(Context ctx) {
        OvkApplication app = (OvkApplication) ctx.getApplicationContext();
        String instance = app.getCurrentInstance();
        long user_id = app.getCurrentUserId();
        return ctx.getDatabasePath(String.format("users_%s_a%s.db", instance, user_id));
    }

    public static String getCurrentDatabaseName(Context ctx) {
        OvkApplication app = (OvkApplication) ctx.getApplicationContext();
        String instance = app.getCurrentInstance();
        long user_id = app.getCurrentUserId();
        return String.format("users_%s_a%s.db", instance, user_id);
    }

    public static ArrayList<Friend> getFriendsList(Context ctx) {
        try {
            Cursor cursor = null;
            CacheOpenHelper helper = new CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx)
            );
            SQLiteDatabase db = helper.getReadableDatabase();
            ArrayList<Friend> result = new ArrayList<>();
            try {
                cursor = db.query("users", null, "is_friend=1",
                        null, null, null, null);
            }  catch (Exception ex) {
                ex.printStackTrace();
            }
            if (cursor != null && cursor.getCount() > 0) {
                int i = 0;
                ContentValues values = new ContentValues();
                cursor.moveToFirst();
                do {
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    Friend friend = new Friend();
                    friend.id = values.getAsLong("id");
                    friend.first_name = values.getAsString("first_name");
                    friend.last_name = values.getAsString("last_name");
                    friend.avatar_url = values.getAsString("photo_small");
                    result.add(friend);
                    i++;
                } while (cursor.moveToNext());
                cursor.close();
                db.close();
                helper.close();
                return result;
            }
            if (cursor != null) {
                cursor.close();
            }
            db.close();
            helper.close();
            return result;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void updateFriendsList(Context ctx, ArrayList<User> users, boolean replace) {
        try {
            CacheOpenHelper helper = new CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx)
            );
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                db.beginTransaction();
                if (replace) {
                    values.put("is_friend", (Boolean) false);
                    db.update("users", values, null, null);
                }
                for (User user : users) {
                    values.clear();
                    values.put("user_id", user.id);
                    values.put("first_name", user.first_name);
                    values.put("last_name", user.last_name);
                    values.put("photo_small", user.avatar_url);
                    values.put("is_friend", true);
                    values.put("sex", user.sex);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                        db.insertWithOnConflict("users", null, values, 5);
                    } else {
                        db.insert("users", null, values);
                    }
                    if (user.birthdate != null && user.birthdate.length() > 0) {
                        values.clear();
                        values.put("id", user.id);
                        String[] bd = user.birthdate.split("\\.");
                        if (bd.length > 1) {
                            values.put("birthday", Integer.parseInt(bd[0]));
                            values.put("birthmonth", Integer.parseInt(bd[1]));
                            if (bd.length > 2) {
                                values.put("birthyear", Integer.parseInt(bd[2]));
                            } else {
                                values.put("birthyear", 0);
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                            db.insertWithOnConflict("users", null, values, 5);
                        } else {
                            db.insert("users", null, values);
                        }
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            db.endTransaction();
            db.close();
            helper.close();
        } catch (Exception ignored) {
        }
    }
}
