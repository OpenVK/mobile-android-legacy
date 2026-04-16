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

package uk.openvk.android.legacy.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;

import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.databases.base.CacheDatabase;

public class UsersCacheDB extends CacheDatabase {

    public static String prefix = "users";

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String db_name) {
            super(ctx, db_name, null, 1);
        }

        public CacheOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            CacheDatabaseTables.createUsersTables(database, false);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
            if (oldVer == 1 && newVer >= oldVer) {
                // TODO: Add database auto-upgrade to new versions
                return;
            }
            onCreate(database);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public SQLiteDatabase getWritableDatabase() {
            while (true) {
                try {
                    SQLiteDatabase db = super.getWritableDatabase();
                    db.setLockingEnabled(false);
                    return db;
                } catch (Exception ex) {
                    try {
                        Thread.sleep(100L);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public SQLiteDatabase getReadableDatabase() {
            while (true) {
                try {
                    SQLiteDatabase db = super.getReadableDatabase();
                    db.setLockingEnabled(false);
                    return db;
                } catch (Exception ex) {
                    try {
                        Thread.sleep(100L);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public static ArrayList<Friend> getFriendsList(Context ctx) {
        try {
            Cursor cursor = null;
            CacheOpenHelper helper = new CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getReadableDatabase();
            ArrayList<Friend> result = new ArrayList<>();
            try {
                cursor = db.query("friends", null, "",
                        null, null, null, null);
            } catch (Exception ex) {
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
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                ContentValues user_values = new ContentValues();
                db.beginTransaction();
                if (replace) {
                    db.update("users", user_values, null, null);
                }
                for (User user : users) {
                    user_values.clear();
                    user_values.put("user_id", user.id);
                    user_values.put("first_name", user.first_name);
                    user_values.put("last_name", user.last_name);
                    user_values.put("photo_small", user.avatar_url);
                    user_values.put("sex", user.sex);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                        db.insertWithOnConflict("users", null,
                                user_values, 5);
                    } else {
                        db.insert("users", null, user_values);
                    }

                    if (user.birthdate != null && user.birthdate.length() > 0) {
                        ContentValues birthday_values = new ContentValues();
                        birthday_values.put("id", user.id);
                        String[] bd = user.birthdate.split("\\.");
                        if (bd.length > 1) {
                            birthday_values.put("bday", Integer.parseInt(bd[0]));
                            birthday_values.put("bmonth", Integer.parseInt(bd[1]));
                            birthday_values.put("byear", bd.length > 2 ? Integer.valueOf(Integer.parseInt(bd[2])) : (Integer) 0);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                            db.insertWithOnConflict("birthdays", null,
                                    birthday_values, 5);
                        } else {
                            db.insert("users", null, birthday_values);
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

    public static ArrayList<User> getUsersList(Context ctx, ArrayList<Integer> ids) {
        try {
            Cursor cursor = null;
            CacheOpenHelper helper = new CacheOpenHelper(
                    ctx.getApplicationContext(), getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getReadableDatabase();
            ArrayList<User> result = new ArrayList<>();
            try {
                cursor = db.query(
                        "users", null, "uid in (" +
                                TextUtils.join(",", ids) + ")",
                        null, null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cursor != null && cursor.getCount() > 0) {
                int i = 0;
                ContentValues values = new ContentValues();
                cursor.moveToFirst();
                do {
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    User user = new User();
                    user.id = values.getAsLong("user_id");
                    user.first_name = values.getAsString("first_name");
                    user.last_name = values.getAsString("last_name");
                    user.avatar_url = values.getAsString("photo_small");
                    user.sex = values.getAsInteger("sex");
                    user.friends_status = values.getAsInteger("is_friend");
                    result.add(user);
                    i++;
                } while (cursor.moveToNext());
                cursor.close();
                db.close();
                helper.close();
                return result;
            }
            if(cursor != null) {
                cursor.close();
            }
            db.close();
            helper.close();
            return result;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean isExist(Context ctx, SQLiteDatabase db, long user_id) {
        boolean result = false;
        try {
            String table_name = "users";
            Cursor cursor = db.query(table_name, new String[]{"count(*)"},
                    "`user_id`=" + user_id,
                    null, null, null, null);
            result = cursor.getCount() > 0 && cursor.moveToFirst() && cursor.getInt(0) > 0;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isExist(Context ctx, long user_id) {
        boolean result = false;
        CacheDatabase.CacheOpenHelper helper =
                new CacheDatabase.CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            String table_name = "users";
            Cursor cursor = db.query(table_name, new String[]{"count(*)"},
                    "`user_id`=" + user_id,
                    null, null, null, null);
            result = cursor.getCount() > 0 && cursor.moveToFirst() && cursor.getInt(0) > 0;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        helper.close();
        return result;
    }

    public static void addUser(Context ctx, User user) {

    }
}
