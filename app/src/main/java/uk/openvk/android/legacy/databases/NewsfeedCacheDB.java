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

public class NewsfeedCacheDB extends CacheDatabase {

    private Context ctx;
    private static String prefix = "posts";
    private static SQLiteDatabase postsDB;
    private static SQLiteDatabase usersDB;
    private static SQLiteDatabase groupsDB;
    private static boolean isInitialized;
    private static CacheOpenHelper postsHelper;
    private static UsersCacheDB.CacheOpenHelper usersHelper;
    private static GroupsCacheDB.CacheOpenHelper groupsHelper;

    public NewsfeedCacheDB(Context ctx) {
        this.ctx = ctx;
    }

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

    public void initDatabases() {

        if(isInitialized)
            return;

        postsHelper = new NewsfeedCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        postsDB = postsHelper.getWritableDatabase();

        usersHelper = new UsersCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, UsersCacheDB.prefix)
        );
        usersDB = usersHelper.getWritableDatabase();

        groupsHelper = new GroupsCacheDB.CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, GroupsCacheDB.prefix)
        );
        groupsDB = groupsHelper.getWritableDatabase();

        isInitialized = true;
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void freeDatabases() {
        postsDB.close();
        usersDB.close();
        groupsDB.close();

        postsHelper.close();
        usersHelper.close();
        groupsHelper.close();

        isInitialized = false;
    }

    public ArrayList<WallPost> getPostsList() {
        try {
            ArrayList<WallPost> posts_result = new ArrayList<>();
            try {
                Cursor posts_cursor = postsDB.rawQuery(
                    "SELECT * "
                       + "FROM newsfeed "
                       + "JOIN wall ON newsfeed.post_id = wall.post_id "
                       + "ORDER BY `time` desc",
                        null
                );

                if (posts_cursor != null && posts_cursor.getCount() > 0) {
                    int i = 0;
                    posts_cursor.moveToFirst();
                    do {
                        WallPost post = new WallPost();
                        post.convertSQLiteToEntity(posts_cursor, ctx);
                        post.resolveAuthorsFromSQLite(usersDB, groupsDB);
                        post.resolveRepost(postsDB, usersDB, groupsDB, ctx);
                        posts_result.add(post);
                        i++;
                    } while (posts_cursor.moveToNext());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return posts_result;
        } catch (Exception e) {
            return null;
        }
    }

    public void clear() {
        try {
            postsDB.delete("wall", null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void putPosts(final ArrayList<WallPost> wallPosts, final boolean clear) {
        new Thread(new Runnable() {
                @Override
                public void run() {
                    if(clear)
                        getPostsDatabase().delete("newsfeed", null, null);

                    try {
                        for (int i = 0; i < wallPosts.size(); i++) {
                            WallPost post = wallPosts.get(i);

                            if(post.getEntityType() == LazyEntity.SLEEPING_ENTITY)
                                continue;

                            if(clear) {
                                ContentValues newsfeed_values = new ContentValues();
                                newsfeed_values.put("post_id", post.post_id);

                                if(post.owner != null) {
                                    newsfeed_values.put("owner_id", post.owner.id);
                                    if(post.author == null)
                                        newsfeed_values.put("author_id", post.owner.id);
                                }

                                if(post.author != null) {
                                    newsfeed_values.put("author_id", post.author.id);
                                    newsfeed_values.put("owner_id", post.author.id);
                                }

                                if(post.dt != null)
                                    newsfeed_values.put("time", post.dt.getTime());
                                else
                                    newsfeed_values.put("time", post.dt_sec * 1000);

                                postsDB.insert("newsfeed", null, newsfeed_values);
                            }

                            if(post.owner != null) {
                                if (WallCacheDB.isExist(postsDB, post.owner.id, post.post_id))
                                    continue;
                            } else {
                                if (WallCacheDB.isExist(postsDB, post.author.id, post.post_id))
                                    continue;
                            }

                            if(post.getEntityType() != LazyEntity.SLEEPING_ENTITY) {
                                post.convertEntityToSQLite(postsDB);
                                if(post.contains_repost) {
                                    post.repost.newsfeed_item.convertEntityToSQLite(postsDB);
                                    writePostAuthorsInfo(ctx, post.repost.newsfeed_item, usersDB, groupsDB);
                                }
                                writePostAuthorsInfo(ctx, post, usersDB, groupsDB);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
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
                    user_values.put("verified", ((User) post.author).verified);
                    users_db.insert("users", null, user_values);
                }
            } else if (post.author instanceof Group) {
                if(!GroupsCacheDB.isExist(ctx, groups_db, post.author.id)) {
                    ContentValues group_values = new ContentValues();
                    group_values.put("group_id", post.author.id);
                    group_values.put("name", ((Group) post.author).name);
                    group_values.put("verified", ((Group) post.author).verified);
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
                    user_values.put("verified", ((User) post.owner).verified);
                    users_db.insert("users", null, user_values);
                }
            } else if (post.owner instanceof Group) {
                if (!GroupsCacheDB.isExist(ctx, groups_db, post.owner.id)) {
                    ContentValues group_values = new ContentValues();
                    group_values.put("group_id", post.owner.id);
                    group_values.put("name", ((Group) post.owner).name);
                    group_values.put("verified", ((Group) post.owner).verified);
                    groups_db.insert("groups", null, group_values);
                }
            }
        }
    }

    public SQLiteDatabase getPostsDatabase() {
        return postsDB;
    }
}
