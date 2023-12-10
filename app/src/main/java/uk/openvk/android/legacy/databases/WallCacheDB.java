package uk.openvk.android.legacy.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.databases.base.CacheDatabase;

/** WallCacheDB class - Wall Cache database control (decompiled from VK 3.x) **/

/*  Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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
 */

public class WallCacheDB extends CacheDatabase {
    private static Semaphore semaphore = new Semaphore(1);

    public static String prefix = "posts";

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String db_name) {
            super(ctx, db_name, null, 1);
        }

        public CacheOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            CacheDatabaseTables.createWallPostTables(database);
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

    public static ArrayList<WallPost> getPostsList(Context ctx) {
        try {
            semaphore.acquire();
            WallCacheDB.CacheOpenHelper helper = new WallCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getReadableDatabase();
            ArrayList<WallPost> result = new ArrayList<>();
            try {
                Cursor cursor = db.query("wall",
                        null, null, null,
                        null, null, "`time` desc");
                if (cursor != null && cursor.getCount() > 0) {
                    int i = 0;
                    cursor.moveToFirst();
                    do {
                        WallPost post = new WallPost();
                        post.convertSQLiteToEntity(cursor, ctx);
                        result.add(post);
                        i++;
                    } while (cursor.moveToNext());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            db.close();
            helper.close();
            semaphore.release();
            return result;
        } catch (Exception e) {
            semaphore.release();
            return null;
        }
    }

    public static void addPost(WallPost post, Context ctx) {
        try {
            WallCacheDB.CacheOpenHelper helper = new WallCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                post.convertEntityToSQLite(db, "wall");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removePost(int owner_id, int post_id, Context ctx) {
        try {
            semaphore.acquire();
            WallCacheDB.CacheOpenHelper helper = new WallCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                db.delete("newsfeed",
                        "`post_id`=" + post_id + " AND `user_id`=" + owner_id, null
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            db.close();
            helper.close();
        } catch (Exception ignored) {
        }
        semaphore.release();
    }

    public static void update(Context ctx, WallPost post) {
        Cursor cursor = null;
        try {
            WallCacheDB.CacheOpenHelper helper = new WallCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getWritableDatabase();
            int flags = 0;
            try {
                cursor = db.query(
                        "news", new String[]{"flags"},
                        "`post_id`=" + post.post_id + " AND `user_id`=" + post.owner_id,
                        null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    flags = cursor.getInt(0);
                    cursor.close();
                } else {
                    if (cursor != null) {
                        cursor.close();
                    }
                    cursor = db.query("news_comments", new String[]{"flags"},
                            "`post_id`=" + post.post_id + " AND `user_id`=" + post.owner_id,
                            null, null, null, null);
                }
                if (flags == 0 && cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    flags = cursor.getInt(0);
                    cursor.close();
                } else {
                    if (cursor != null) {
                        cursor.close();
                    }
                    cursor = db.query("wall", new String[]{"flags"},
                            "`post_id`=" + post.post_id + " AND `user_id`=" + post.owner_id,
                            null, null, null, null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (flags == 0 && cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int flags2 = cursor.getInt(0);
                cursor.close();
                ContentValues values = new ContentValues();
                values.put("likes", post.counters.likes);
                values.put("comments", post.counters.comments);
                int flags3 = post.counters.isLiked ? flags2 | 8 : flags2 & (-9);
                values.put("flags", post.repost != null ? flags3 | 4 : flags3 & (-5));
                db.update("news", values, "`post_id`=" +
                        post.post_id + " AND `user_id`=" + post.owner_id, null);
                db.update("news_comments", values,
                        "`post_id`=" + post.post_id + " AND `user_id`=" + post.owner_id, null);
                db.update("wall", values, "`post_id`=" +
                        post.post_id + " AND `user_id`=" + post.owner_id, null);
                db.close();
                helper.close();
                return;
            }
            if (cursor != null) {
                cursor.close();
            }
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
