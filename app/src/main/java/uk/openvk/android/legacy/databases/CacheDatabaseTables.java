package uk.openvk.android.legacy.databases;

import android.database.sqlite.SQLiteDatabase;

/** CacheDatabaseTables class - DB tables controls **/

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

public class CacheDatabaseTables {

    public static void createWallPostTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `newsfeed`");
        db.execSQL("DROP TABLE IF EXISTS `newsfeed_comments`");

        db.execSQL(
                "CREATE TABLE `newsfeed` (" +
                        "post_id bigint, " +
                        "author_id bigint, " +
                        "owner_id bigint, " +
                        "author_name varchar(150) not null, " +
                        "avatar_url varchar(400) not null, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "reposts int, " +
                        "attachments blob, " +
                        "contains_repost bit, " +
                        "repost_original_id bigint, " +
                        "repost_owner_id bigint, " +
                        "repost_author_id bigint, " +
                        "repost_original_time bigint, " +
                        "repost_author_name varchar(150), " +
                        "repost_avatar_url varchar(400), " +
                        "repost_text text, " +
                        "repost_attachments blob" +
                ")"
                );
        db.execSQL(
                "CREATE TABLE `newsfeed_comments` (" +
                        "post_id bigint, " +
                        "user_id bigint, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "username varchar(150) not null, " +
                        "avatar_url varchar(360), " +
                        "repost_userid int, " +
                        "repost_username varchar(150), " +
                        "attachments blob, " +
                        "contains_repost bit, " +
                        "repost_text text, " +
                        "repost_avatar_url varchar(360), " +
                        "repost_orig_id bigint, " +
                        "repost_orig_time bigint, " +
                        "reposts int " +
                 ")"
                );
        db.execSQL("DROP TABLE IF EXISTS `wall`");
        db.execSQL("DROP TABLE IF EXISTS `feed_lists`");
        db.execSQL(
                "CREATE TABLE `wall` (" +
                        "post_id bigint, " +
                        "author_id bigint, " +
                        "owner_id bigint, " +
                        "author_name varchar(150) not null, " +
                        "avatar_url varchar(400) not null, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "reposts int, " +
                        "attachments blob, " +
                        "contains_repost bit, " +
                        "repost_original_id bigint, " +
                        "repost_owner_id bigint, " +
                        "repost_author_id bigint, " +
                        "repost_original_time bigint, " +
                        "repost_author_name varchar(150), " +
                        "repost_avatar_url varchar(400), " +
                        "repost_text text, " +
                        "repost_attachments blob" +
                 ")"
                );
        db.execSQL(
                "CREATE TABLE `feed_lists` (lists_id int not null, title varchat(500))"
        );
    }

    public static void createFriendsTable(SQLiteDatabase db) {
        db.execSQL(
                "DROP TABLE IF EXISTS `friendlist`"
        );
        db.execSQL(
                "CREATE TABLE `friendlist` (" +
                        "user_id bigint unique, " +
                        "first_name varchar(150), " +
                        "last_name varchar(150), " +
                        "photo varchar(200), " +
                        "birthday int, " +
                        "birthmonth int, " +
                        "birthyear int, " +
                        "name_r varchar(200)" +
                ")"
        );
    }

    public static void createConversationsTable(SQLiteDatabase db) {
        db.execSQL(
                "DROP TABLE IF EXISTS `dialogslist`"
        );
        db.execSQL("CREATE TABLE `dialogslist` (" +
                        "user_id bigint, " +
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

    public static void createAudioTracksTable(SQLiteDatabase db) {
        db.execSQL(
                "DROP TABLE IF EXISTS `tracks`"
        );
        db.execSQL(
                "CREATE TABLE `tracks` (" +
                        "owner_id bigint, " +
                        "audio_id bigint, " +
                        "title varchar(500), " +
                        "artist varchar(500), " +
                        "duration int, " +
                        "lastplay int, " +
                        "user bool, " +
                        "lyrics text, " +
                        "url varchar(700), " +
                        "status int" +
                ")"
        );
    }

    public static void createMainCacheTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `birthdays`");
        db.execSQL("DROP TABLE IF EXISTS `messages`");
        db.execSQL("DROP TABLE IF EXISTS `conversations`");
        db.execSQL("DROP TABLE IF EXISTS `conversations_users`");
        db.execSQL("DROP VIEW IF EXISTS `dialogs`");
        db.execSQL("DROP TABLE IF EXISTS `groups`");
        db.execSQL("DROP TABLE IF EXISTS `newsfeed`");
        db.execSQL("DROP TABLE IF EXISTS `newsfeed_comments`");
        db.execSQL("DROP TABLE IF EXISTS `wall`");
        db.execSQL("DROP TABLE IF EXISTS `wall_drafts`");
        db.execSQL("DROP TABLE IF EXISTS `api_queues`");
        db.execSQL("CREATE TABLE `users` (" +
                "user_id int not null unique, " +
                "first_name varchar(150), " +
                "last_name varchar(150), " +
                "photo_small varchar(200), " +
                "photo_big varchar(200), " +
                "is_friend bool, " +
                "sex bool)");
        db.execSQL("CREATE TABLE `birthdays` (" +
                "user_id int unique, " +
                "name_r varchar(150), " +
                "bday int not null, " +
                "bmonth int not null, " +
                "byear int not null" +
                ")");
        db.execSQL("CREATE TABLE `messages` (" +
                "msg_id int unique, " +
                "peer int not null, " +
                "sender int not null, " +
                "text text, " +
                "time int not null, " +
                "attachments blob, " +
                "fwd blob, " +
                "flags int not null" +
                ")");
        db.execSQL("CREATE TABLE `conversations` (" +
                "cid int unique, " +
                "title varchar(500), " +
                "admin int not null, " +
                "photo varchar(500), " +
                "need_update_users bool not null default 1" +
                ")");
        db.execSQL("CREATE TABLE `conversations_users` (" +
                "cid int not null, " +
                "uid int not null, " +
                "inviter int not null, " +
                "invited int not null" +
                ")");
        db.execSQL("CREATE VIEW `conversations2` " +
                "AS SELECT messages.*, users.*, conversations.* FROM messages " +
                "JOIN messages AS msg2 ON messages.msg_id=msg2.mid " +
                "LEFT JOIN users ON messages.peer=user_id " +
                "LEFT JOIN converstaions ON -messages.peer=cid " +
                "GROUP BY messages.peer HAVING messages.time=max(msg2.time)");
        db.execSQL("CREATE INDEX `messages_msgid` ON messages (peer)");
        db.execSQL("CREATE INDEX `messages_time` ON messages (time)");
        db.execSQL("CREATE INDEX `conversations_users_cid` ON conversation_users (cid)");
        db.execSQL("CREATE TABLE `groups` (" +
                "group_id int not null unique, " +
                "name varchar(200), " +
                "activity varchar(200), " +
                "count int not null, " +
                "type int not null, " +
                "closed int not null" +
                ")");
        db.execSQL("CREATE TABLE `api_queues` (" +
                "id INTEGER PRIMARY KEY, " +
                "method varchar(200), " +
                "args varchar(400), " +
                "_where varchar(25)" +
                ")");
    }
}
