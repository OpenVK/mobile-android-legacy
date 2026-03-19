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

import android.database.sqlite.SQLiteDatabase;

public class CacheDatabaseTables {

    public static void createWallPostTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `newsfeed`");
        db.execSQL("DROP TABLE IF EXISTS `newsfeed_comments`");

        db.execSQL(
                "CREATE TABLE `newsfeed` (" +
                        "post_id bigint, " +
                        "time bigint, " +
                        "FOREIGN KEY(post_id) REFERENCES wall(post_id)," +
                        "FOREIGN KEY(time) REFERENCES wall(time)" +
                ")"
                );
        db.execSQL(
                "CREATE TABLE `wall_comments` (" +
                        "post_id bigint, " +
                        "user_id bigint, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "username varchar(150) not null, " +
                        "avatar_url varchar(360), " +
                        "attachments blob, " +
                        "contains_repost bit, " +
                        "repost_id bigint, " +
                        "reposts int, " +
                        "FOREIGN KEY(repost_id) REFERENCES wall(post_id)" +
                 ")"
                );
        db.execSQL("DROP TABLE IF EXISTS `wall`");
        db.execSQL("DROP TABLE IF EXISTS `feed_lists`");
        db.execSQL(
                "CREATE TABLE `wall` (" +
                        "post_id bigint, " +
                        "author_id bigint, " +
                        "owner_id bigint, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "reposts int, " +
                        "attachments blob, " +
                        "contains_repost bit, " +
                        "repost_id bigint, " +
                        "FOREIGN KEY(repost_id) REFERENCES wall(post_id)" +
                 ")"
                );
        db.execSQL(
                "CREATE TABLE `feed_lists` (lists_id int not null, title varchat(500))"
        );
    }

    public static void createUsersTables(SQLiteDatabase db, boolean clear) {
        if(clear) {
            db.execSQL("DROP TABLE IF EXISTS `users`");
            db.execSQL("DROP TABLE IF EXISTS `birthdays`");
            db.execSQL("DROP TABLE IF EXISTS `friends`");
        }

        db.execSQL(
                "CREATE TABLE `users` (" +
                        "user_id bigint PRIMARY KEY, " +
                        "first_name varchar(150) NOT NULL, " +
                        "last_name varchar(150), " +
                        "screenname varchar(150), " +
                        "photo varchar(360), " +
                        "photo_small varchar(360), " +
                        "sex int NOT NULL, " +
                        "name_r varchar(200)" +
                ")"
        );
        db.execSQL(
                "CREATE TABLE `birthdays` (" +
                        "user_id bigint unique," +
                        "bday int," +
                        "bmonth int," +
                        "byear int," +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                ")"
        );
        db.execSQL(
                "CREATE TABLE `friends` (" +
                    "user_id bigint, " +
                    "user2_id bigint, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id)," +
                    "FOREIGN KEY(user2_id) REFERENCES users(user_id)" +
                ")"
        );
    }

    public static void createGroupsTable(SQLiteDatabase db, boolean clear) {
        if(clear) {
            db.execSQL("DROP TABLE IF EXISTS `groups`");
        }

        db.execSQL(
                "CREATE TABLE `groups` (" +
                        "group_id bigint PRIMARY KEY, " +
                        "name varchar(200) NOT NULL, " +
                        "screenname varchar(150), " +
                        "description varchar(600)," +
                        "photo varchar(360), " +
                        "admin bit, " +
                        "type int, " +
                        "members bigint" +
                        ")"
        );
    }

    public static void createConversationsTable(SQLiteDatabase db) {
        db.execSQL(
                "DROP TABLE IF EXISTS `conversations`"
        );
        db.execSQL("CREATE TABLE `conversations` (" +
                        "peer_id bigint, " +
                        "photo varchar(500), " +
                        "title varchar(500), " +
                        "lastmsg varchar(500), " +
                        "time int, " +
                        "readstate bool, " +
                        "attach_type int, " +
                        "photo2 varchar(500)" +
                    ")"
        );
    }

    public static void createAudioTracksTable(SQLiteDatabase db, boolean clear) {
        if(clear) {
            db.execSQL(
                    "DROP TABLE IF EXISTS `audios`"
            );
            db.execSQL(
                    "DROP TABLE IF EXISTS `playlists`"
            );
            db.execSQL(
                    "DROP TABLE IF EXISTS `relations`"
            );
        }
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS `audios` (" +
                        "owner_id bigint, " +
                        "audio_id bigint, " +
                        "title varchar(500), " +
                        "artist varchar(500), " +
                        "duration int, " +
                        "lastplay int, " +
                        "user bit, " +
                        "lyrics bigint, " +
                        "url varchar(700), " +
                        "status int" +
                ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS `playlists` (" +
                        "playlist_id NOT NULL PRIMARY KEY, " +
                        "owner_id NOT NULL, " +
                        "title varchar(150) NOT NULL, " +
                        "description varchar(150) NOT NULL " +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS `relations` (" +
                        "id NOT NULL PRIMARY KEY, " +
                        "audio_id bigint, " +
                        "playlist_id bigint," +
                        "FOREIGN KEY(audio_id) REFERENCES audios(audio_id)," +
                        "FOREIGN KEY(playlist_id) REFERENCES playlists(playlist_id)" +
                        ")"
        );
    }
}
