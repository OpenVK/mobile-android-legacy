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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Vector;

import uk.openvk.android.client.entities.Audio;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.databases.base.CacheDatabase;
import uk.openvk.android.legacy.services.AudioPlayerService;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AudioCacheDB extends CacheDatabase {
    public static Vector<String> cachedIDs = new Vector<>();
    public static Vector<String> cacheReqs = new Vector<>();
    private static Context ctx;
    public static String prefix = "audios";

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String db_name) {
            super(ctx, db_name, null, 1);
        }

        public CacheOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            CacheDatabaseTables.createAudioTracksTable(database, false);
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

    public void putTrack(Context ctx, Audio track, boolean forced, boolean intoSearchResults) {
        CacheOpenHelper helper2 = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
        SQLiteDatabase db2 = helper2.getWritableDatabase();
        if (!isExist(ctx, track, intoSearchResults)) {
            Intent intent = new Intent(AudioPlayerService.ACTION_UPDATE_PLAYLIST);
            ctx.getApplicationContext().sendBroadcast(intent);
            try {
                ContentValues values2 = new ContentValues();
                values2.put("audio_id", track.id);
                values2.put("owner_id", track.owner_id);
                values2.put("artist", track.artist);
                values2.put("duration", track.getDurationInSeconds());
                values2.put("lastplay", System.currentTimeMillis() / 1000);
                values2.put("user", forced);
                values2.put("lyrics", track.lyrics);
                String table_name = "tracks";
                if(intoSearchResults) {
                    table_name = "search_results";
                }
                db2.insert(table_name, null, values2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (forced) {
            CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put("user", true);
                String table_name = "tracks";
                if(intoSearchResults) {
                    table_name = "search_results";
                }
                db.update(table_name, values,
                        "audio_id=" + track.id
                                + " and owner_id=" + track.owner_id,
                        null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db2.close();
        helper2.close();
    }

    private static boolean isExist(Context ctx, Audio track, boolean inSearchResults) {
        boolean result = false;
        CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            String table_name = "audios";

            if(inSearchResults)
                table_name = "search_results";

            Cursor cursor = db.query(table_name, new String[]{"count(*)"},
                    "`audio_id`=? and `sender_id`=?",
                    new String[]{
                            String.valueOf(track.id),
                            String.valueOf(track.sender.id)
                    }, null, null, null);

            int count = 0;

            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            result = count > 0;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        helper.close();
        return result;
    }

    private static boolean isRelationExist(Context ctx, Audio track, long owner_id) {
        boolean result = false;
        CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            String table_name = "relations";

            Cursor cursor = db.query(table_name, new String[]{"count(*)"},
                    "`audio_id`=? and `sender_id`=? and `owner_id`=?",
                    new String[]{
                                    String.valueOf(track.id),
                                    String.valueOf(track.sender.id),
                                    String.valueOf(owner_id)
                    },
                    null, null, null);
            int count = 0;

            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }

            result = count > 0;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = true;
        }
        db.close();
        helper.close();
        return result;
    }

    public static void fillDatabase(final Context ctx2,
                                    final ArrayList<Audio> audios,
                                    final boolean clear) {

        new Thread(new Runnable() {
                @Override
                public void run() {
                    final AudioCacheDB.CacheOpenHelper helper =
                            new AudioCacheDB.CacheOpenHelper(ctx2, getCurrentDatabaseName(ctx2, prefix));
                    SQLiteDatabase db = helper.getWritableDatabase();
                    try {
                        String table_name = "audios";
                        String table_name2 = "relations";

                        for (int i = 0; i < audios.size(); i++) {
                            Audio track = audios.get(i);
                            ContentValues values = new ContentValues();
                            if(!isExist(ctx, track, false)) {
                                    values.put("audio_id", track.id);
                                    values.put("sender_id", track.sender.id);
                                    values.put("title", track.title);
                                    values.put("artist", track.artist);
                                    values.put("duration", track.getDurationInSeconds());
                                    values.put("lastplay", 0);
                                    values.put("user", true);
                                    values.put("lyrics", track.lyrics);
                                    values.put("url", track.url);
                                    values.put("status", track.status);
                                    db.insert(table_name, null, values);
                            }

                            if(!isRelationExist(ctx, track, track.owner_id)) {
                                ContentValues values2 = new ContentValues();
                                values2.put("relation_id", i + 1);
                                values2.put("audio_id", track.id);
                                values2.put("sender_id", track.sender.id);
                                values2.put("owner_id", track.owner_id);
                                values2.put("playlist_id", -3);
                                db.insert(table_name2, null, values2);
                            }
                        }
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                    }
                    db.close();
                    helper.close();
                }
            }).start();
    }

    public static void fillDatabaseFromWall(Context ctx2, ArrayList<Audio> audios,
                                            long post_id, boolean clear) {
        final AudioCacheDB.CacheOpenHelper helper =
                new AudioCacheDB.CacheOpenHelper(ctx2, getCurrentDatabaseName(ctx2, prefix));

        Cursor cursor = null;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            if (clear) {
                cachedIDs.clear();
            }
            CacheDatabaseTables.createAudioTracksTable(db, clear);
            cursor = db.query("audios", new String[]{"owner_id", "audio_id"},
                    null, null, null, null, null);
            cursor.moveToFirst();

            for (int i = 0; i < audios.size(); i++) {
                Audio track = audios.get(i);
                ContentValues values = new ContentValues();
                values.put("audio_id", track.id);
                values.put("sender_id", track.sender.id);
                values.put("title", track.title);
                values.put("artist", track.artist);
                values.put("duration", track.getDurationInSeconds());
                values.put("lastplay", 0);
                values.put("user", true);
                values.put("lyrics", track.lyrics);
                values.put("url", track.url);
                values.put("status", track.status);
                db.insert("audios", null, values);
                String track_name = String.format("%s_%s", track.id, track.owner_id);
                cachedIDs.add(track_name);
            }
            helper.close();
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
    }

    public static ArrayList<Audio> getCachedAudiosList(Context ctx2, long owner_id, boolean fromSearchResults) {
        final AudioCacheDB.CacheOpenHelper helper =
                new AudioCacheDB.CacheOpenHelper(ctx2, getCurrentDatabaseName(ctx2, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        ArrayList<Audio> list = new ArrayList<>();
        try {
            String table_name = "relations";

            Cursor cursor = db.rawQuery(
                    "SELECT relations.audio_id, relations.sender_id, " +
                                "relations.owner_id, audios.title, audios.artist, " +
                                "audios.duration, audios.lastplay, audios.url, audios.status " +
                         "FROM relations " +
                         "JOIN audios ON relations.audio_id = audios.audio_id " +
                         "AND relations.sender_id = audios.sender_id " +
                         "WHERE owner_id = ?" +
                         "ORDER BY relation_id ASC",
                    new String[]{String.valueOf(owner_id)}
            );
            cursor.moveToFirst();
            int i = 0;
            do {
                Audio track = new Audio();
                track.sender = new User();
                track.id = cursor.getLong(0);
                track.sender.id = cursor.getLong(1);
                track.owner_id = cursor.getLong(2);
                track.title = cursor.getString(3);
                track.artist = cursor.getString(4);
                track.setDuration(cursor.getInt(5));
                track.lyrics = cursor.getLong(6);
                track.url = cursor.getString(7);
                list.add(track);
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        helper.close();
        ctx = ctx2;
        deleteOldTrack(ctx);
        return list;
    }

    private static void deleteOldTrack(Context ctx) {
        final AudioCacheDB.CacheOpenHelper helper =
                new AudioCacheDB.CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cursor = db.query("audios",
                    null, "user=0", null, null, null,
                    "lastplay asc");
            cursor.moveToFirst();
            if (cursor.getCount() > 10) {
                int nDel = cursor.getCount() - 10;
                String where = "";
                for (int i = 0; i < nDel; i++) {
                    where = String.valueOf(where) + "or(owner_id=" + cursor.getInt(0) +
                            " AND audio_id=" + cursor.getInt(1) + ")";
                    cachedIDs.remove(String.valueOf(cursor.getInt(0)) + "_" + cursor.getInt(1));
                    cursor.moveToNext();
                }
                cursor.close();
                db.delete("tracks", where.substring(2), null);
                Intent intent = new Intent(AudioPlayerService.ACTION_UPDATE_PLAYLIST);
                intent.putExtra("reload_cached_list", true);
                ctx.getApplicationContext().sendBroadcast(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        helper.close();
    }

    public static void clear(Context ctx, boolean fromSearchOnly) {
        if(getCurrentDatabaseName(ctx, prefix) != null) {
            final AudioCacheDB.CacheOpenHelper helper =
                    new AudioCacheDB.CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                db.delete("audios", null, null);
                cachedIDs.clear();
                Intent intent = new Intent(AudioPlayerService.ACTION_UPDATE_PLAYLIST);
                intent.putExtra("reload_cached_list", true);
                ctx.getApplicationContext().sendBroadcast(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            db.close();
            helper.close();
        }
    }

    public static void updatePlayTime(Context ctx, int owner_id, int audio_id) {
        CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("lastplay", (System.currentTimeMillis() / 1000));
            db.update("files", values,
                    "audio_id=" + audio_id + " and owner_id=" + owner_id,
                    null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        helper.close();
    }

    public static ArrayList<Audio> getAudiosListFromWall(Context ctx2, long post_id) {
        final AudioCacheDB.CacheOpenHelper helper =
                new AudioCacheDB.CacheOpenHelper(ctx2, getCurrentDatabaseName(ctx2, prefix));
        SQLiteDatabase db = helper.getWritableDatabase();
        ArrayList<Audio> list = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT * "
                            + "FROM audios "
                            + "JOIN wall_audios ON wall_audios.post_id = audios.post_id "
                            + "WHERE post_id = ? ORDER BY `time` desc ",
                    new String[]{Long.toString(post_id)}
            );
            cursor.moveToFirst();
            int i = 0;
            do {
                Audio track = new Audio();
                track.sender.id = cursor.getLong(0);
                track.id = cursor.getLong(1);
                track.title = cursor.getString(2);
                track.artist = cursor.getString(3);
                track.setDuration(cursor.getInt(4));
                track.lyrics = cursor.getLong(8);
                track.url = cursor.getString(9);
                list.add(track);
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();
        helper.close();
        ctx = ctx2;
        deleteOldTrack(ctx);
        return list;
    }
}
