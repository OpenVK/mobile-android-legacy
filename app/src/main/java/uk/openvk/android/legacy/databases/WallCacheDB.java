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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.legacy.databases.base.CacheDatabase;

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

    public static ArrayList<WallPost> getPostsList(Context ctx, long owner_id) {
        try {
            semaphore.acquire();
            WallCacheDB.CacheOpenHelper posts_helper = new WallCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase posts_db = posts_helper.getReadableDatabase();

            UsersCacheDB.CacheOpenHelper users_helper = new UsersCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, UsersCacheDB.prefix)
            );
            SQLiteDatabase users_db = users_helper.getReadableDatabase();

            GroupsCacheDB.CacheOpenHelper groups_helper = new GroupsCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, GroupsCacheDB.prefix)
            );
            SQLiteDatabase groups_db = groups_helper.getReadableDatabase();

            ArrayList<WallPost> posts_result = new ArrayList<>();
            try {
                Cursor posts_cursor = posts_db.rawQuery(
                        "SELECT * "
                                + "FROM wall WHERE owner_id = ?"
                                + "ORDER BY `time` desc",
                        new String[] {Long.toString(owner_id)}
                );

                if (posts_cursor != null && posts_cursor.getCount() > 0) {
                    int i = 0;
                    posts_cursor.moveToFirst();
                    do {
                        WallPost post = new WallPost();
                        post.convertSQLiteToEntity(posts_cursor, ctx);
                        post.resolveRepost(posts_db, users_db, groups_db, ctx);
                        post.resolveAuthorsFromSQLite(users_db, groups_db);
                        posts_result.add(post);
                        i++;
                    } while (posts_cursor.moveToNext());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            posts_db.close();
            posts_helper.close();

            users_db.close();
            users_helper.close();

            groups_db.close();
            groups_helper.close();

            semaphore.release();
            return posts_result;
        } catch (Exception e) {
            semaphore.release();
            return null;
        }
    }

    public static void addPost(WallPost post, Context ctx) {
        try {
            NewsfeedCacheDB.CacheOpenHelper posts_helper = new NewsfeedCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase posts_db = posts_helper.getWritableDatabase();

            UsersCacheDB.CacheOpenHelper users_helper = new UsersCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, UsersCacheDB.prefix)
            );
            SQLiteDatabase users_db = users_helper.getWritableDatabase();

            GroupsCacheDB.CacheOpenHelper groups_helper = new GroupsCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, GroupsCacheDB.prefix)
            );

            SQLiteDatabase groups_db = groups_helper.getWritableDatabase();

            try {
                post.convertEntityToSQLite(posts_db);
                post.resolveAuthorsFromSQLite(users_db, groups_db);
                post.resolveRepost(posts_db, users_db, groups_db, ctx);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            posts_db.close();
            posts_helper.close();
            users_db.close();
            users_helper.close();
            groups_db.close();
            groups_helper.close();
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
                db.delete("wall",
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
                        "`post_id`=" + post.post_id + " AND `user_id`=" + post.owner.id,
                        null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    flags = cursor.getInt(0);
                    cursor.close();
                } else {
                    if (cursor != null)
                        cursor.close();
                    cursor = db.query("news_comments", new String[]{"flags"},
                            "`post_id`=" + post.post_id + " AND `user_id`=" + post.owner.id,
                            null, null, null, null);
                }
                if (flags == 0 && cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    flags = cursor.getInt(0);
                    cursor.close();
                } else {
                    if (cursor != null)
                        cursor.close();
                    cursor = db.query("wall", new String[]{"flags"},
                            "`post_id`=" + post.post_id + " AND `user_id`=" + post.owner.id,
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
                db.update("newsfeed", values, "`post_id`=" +
                        post.post_id + " AND `user_id`=" + post.owner.id, null);
                db.update("newsfeed_comments", values,
                        "`post_id`=" + post.post_id + " AND `user_id`=" +
                                post.owner.id, null);
                db.update("wall", values, "`post_id`=" +
                        post.post_id + " AND `user_id`=" + post.owner.id, null);
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

    public static void putPosts(Context ctx, ArrayList<WallPost> wallPosts,
                                long owner_id, boolean clear) {
        try {
            WallCacheDB.CacheOpenHelper posts_helper = new WallCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase posts_db = posts_helper.getWritableDatabase();

            UsersCacheDB.CacheOpenHelper users_helper = new UsersCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, UsersCacheDB.prefix)
            );
            SQLiteDatabase users_db = users_helper.getWritableDatabase();

            GroupsCacheDB.CacheOpenHelper groups_helper = new GroupsCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, GroupsCacheDB.prefix)
            );

            SQLiteDatabase groups_db = groups_helper.getWritableDatabase();

            if(clear) {
                posts_db.delete("wall", "owner_id = ?", new String[]{
                        Long.toString(owner_id)
                });
            }

            try {
                for (int i = 0; i < wallPosts.size(); i++) {
                    WallPost post = wallPosts.get(i);
                    post.convertEntityToSQLite(posts_db);
                    if(post.contains_repost) {
                        post.repost.newsfeed_item.convertEntityToSQLite(posts_db);
                        writePostAuthorsInfo(ctx, post.repost.newsfeed_item, users_db, groups_db);
                    }
                    writePostAuthorsInfo(ctx, post, users_db, groups_db);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            posts_db.close();
            posts_helper.close();
            users_db.close();
            users_helper.close();
            groups_db.close();
            groups_helper.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void writePostAuthorsInfo(Context ctx, WallPost post, SQLiteDatabase users_db, SQLiteDatabase groups_db) {
        if (post.author != null) {
            if (post.author instanceof User) {
                if(!UsersCacheDB.isExist(ctx, users_db, post.author.id)) {
                    ContentValues user_values = new ContentValues();
                    user_values.put("user_id", post.author.id);
                    user_values.put("first_name", ((User) post.author).first_name);
                    user_values.put("last_name", ((User) post.author).last_name);
                    user_values.put("sex", ((User) post.author).sex);
                    users_db.insert("users", null, user_values);
                }
            } else if (post.author instanceof Group) {
                if(!GroupsCacheDB.isExist(ctx, groups_db, post.author.id)) {
                    ContentValues group_values = new ContentValues();
                    group_values.put("group_id", post.author.id);
                    group_values.put("name", ((Group) post.author).name);
                    groups_db.insert("groups", null, group_values);
                }
            }
        }

        if (post.owner != null) {
            if (post.owner instanceof User) {
                if (!UsersCacheDB.isExist(ctx, users_db, post.owner.id)) {
                    ContentValues user_values = new ContentValues();
                    user_values.put("user_id", post.owner.id);
                    user_values.put("first_name", ((User) post.owner).first_name);
                    user_values.put("last_name", ((User) post.owner).last_name);
                    user_values.put("sex", ((User) post.owner).sex);
                    users_db.insert("users", null, user_values);
                }
            } else if (post.owner instanceof Group) {
                if (!GroupsCacheDB.isExist(ctx, groups_db, post.owner.id)) {
                    ContentValues group_values = new ContentValues();
                    group_values.put("group_id", post.owner.id);
                    group_values.put("name", ((Group) post.owner).name);
                    groups_db.insert("groups", null, group_values);
                }
            }
        }
    }

    public static void clear(long owner_id, Context ctx) {
        try {
            WallCacheDB.CacheOpenHelper helper = new WallCacheDB.CacheOpenHelper(
                    ctx.getApplicationContext(),
                    getCurrentDatabaseName(ctx, prefix)
            );
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("wall", "owner_id = ?", new String[]{
                    Long.toString(owner_id)
            });
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isExist(SQLiteDatabase db, long owner_id, long post_id) {
        boolean result = false;
        try {
            String table_name = "wall";
            Cursor cursor = db.query(table_name, new String[]{"count(*)"},
                    "`owner_id` = ? AND `post_id` = ?",
                    new String[] {Long.toString(owner_id), Long.toString(post_id)},
                    null, null, null);
            result = cursor.getCount() > 0 && cursor.moveToFirst() && cursor.getInt(0) > 0;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
