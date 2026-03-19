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

import java.util.List;
import java.util.Vector;

import uk.openvk.android.client.entities.Group;
import uk.openvk.android.legacy.databases.base.CacheDatabase;

public class GroupsCacheDB extends CacheDatabase {

    public static String prefix = "groups";

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String db_name) {
            super(ctx, db_name, null, 1);
        }

        public CacheOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            CacheDatabaseTables.createGroupsTable(database, false);
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

    public static Vector<Group> getList(Context ctx) {
        NewsfeedCacheDB.CacheOpenHelper helper = new NewsfeedCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getReadableDatabase();
        Vector<Group> result = new Vector<>();
        try {
            Cursor cursor = db.query("groups",
                    null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ContentValues values = new ContentValues();
                do {
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    Group group = new Group();
                    group.id = values.getAsLong("id");
                    group.name = values.getAsString("name");
                    group.avatar_url = values.getAsString("photo");
                    result.add(group);
                } while (cursor.moveToNext());
            }
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static Vector<Group> get(Context ctx, int count) {
        NewsfeedCacheDB.CacheOpenHelper helper = new NewsfeedCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getReadableDatabase();
        Vector<Group> result = new Vector<>();
        int i = 0;
        try {
            Cursor cursor = db.query("groups",
                    null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ContentValues values = new ContentValues();
                do {
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    Group group = new Group();
                    group.id = values.getAsInteger("id");
                    group.name = values.getAsString("name");
                    group.avatar_url = values.getAsString("photo");

                    result.add(group);
                    i++;
                } while (cursor.moveToNext());
            }
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void replace(Context ctx, List<Group> groups) {
        NewsfeedCacheDB.CacheOpenHelper helper = new NewsfeedCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = null;
        try {
            try {
                db = helper.getWritableDatabase();
                db.beginTransaction();
                db.delete("groups", null, null);
                for (Group group : groups) {
                    ContentValues values = new ContentValues();
                    values.put("id", group.id);
                    values.put("name", group.name);
                    values.put("photo", group.avatar_url);
                    db.insert("groups", null, values);
                }
                db.setTransactionSuccessful();
            } catch (Exception x) {
                if (db != null) {
                    db.endTransaction();
                }
            }
            if (db != null) {
                db.close();
            }
            helper.close();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public static void add(Context ctx, Group group) {
        NewsfeedCacheDB.CacheOpenHelper helper = new NewsfeedCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("id", group.id);
            values.put("name", group.name);
            values.put("photo", group.avatar_url);
            db.insert("groups", null, values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        helper.close();
    }

    public static void remove(Context ctx, int group_id) {
        NewsfeedCacheDB.CacheOpenHelper helper = new NewsfeedCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.delete("groups", "`id`=" + group_id, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        helper.close();
    }

    public static boolean hasEntries(Context ctx) {
        try {
            NewsfeedCacheDB.CacheOpenHelper helper = new NewsfeedCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM `groups`", null);
            cursor.moveToFirst();
            boolean result = cursor.getInt(0) > 0;
            cursor.close();
            db.close();
            helper.close();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isExist(Context ctx, SQLiteDatabase db, long group_id) {
        boolean result = false;
        try {
            String table_name = "groups";
            Cursor cursor = db.query(table_name, new String[]{"count(*)"},
                    "`group_id`=" + group_id,
                    null, null, null, null);
            result = cursor.getCount() > 0 && cursor.moveToFirst() && cursor.getInt(0) > 0;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isExist(Context ctx, long groups_id) {
        boolean result = false;
        CacheDatabase.CacheOpenHelper helper =
                new CacheDatabase.CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            String table_name = "groups";
            Cursor cursor = db.query(table_name, new String[]{"count(*)"},
                    "`group_id`=" + groups_id,
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
}
