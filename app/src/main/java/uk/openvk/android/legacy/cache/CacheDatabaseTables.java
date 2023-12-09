package uk.openvk.android.legacy.cache;

import android.database.sqlite.SQLiteDatabase;

public class CacheDatabaseTables {

    public static void createPostsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `news`");
        db.execSQL("DROP TABLE IF EXISTS `news_comments`");
        db.execSQL("DROP TABLE IF EXISTS `wall`");
        db.execSQL("DROP TABLE IF EXISTS `feed_lists`");
        db.execSQL(
                "CREATE TABLE `news` (" +
                        "post_id bigint, " +
                        "user_id bigint, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "username varchar(150) not null, " +
                        "userphoto varchar(400) not null, " +
                        "retweet_uid int, " +
                        "retweet_username varchar(400), " +
                        "attachments blob, " +
                        "flags int, " +
                        "retweet_text text, " +
                        "retweet_attachments blob, " +
                        "retweet_orig_id bigint, " +
                        "retweet_orig_time bigint, " +
                        "retweets int" +
                ")"
                );
        db.execSQL(
                "CREATE TABLE `news_comments` (" +
                        "post_id bigint, " +
                        "user_id bigint, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "username varchar(150) not null, " +
                        "userphoto varchar(200) not null, " +
                        "retweet_uid int, " +
                        "retweet_username varchar(150), " +
                        "attachments blob, " +
                        "flags int, " +
                        "retweet_text text, " +
                        "retweet_user_photo varchar(150), " +
                        "retweet_orig_id bigint, " +
                        "retweet_orig_time bigint, " +
                        "retweets int, " +
                        "last_comment_name varchar(150), " +
                        "last_comment_photo varchar(200), " +
                        "last_comment_text text, " +
                        "last_comment_time int not null" +
                 ")"
                );
        db.execSQL(
                "CREATE TABLE `wall` (" +
                        "post_id bigint, " +
                        "user_id bigint, " +
                        "text text, " +
                        "time bigint, " +
                        "likes int, " +
                        "comments int, " +
                        "username varchar(150) not null, " +
                        "userphoto varchar(200) not null, " +
                        "retweet_user_id bigint, " +
                        "retweet_username varchar(150), " +
                        "attachments blob, " +
                        "flags int, " +
                        "retweet_text text, " +
                        "retweet_user_photo varchar(150), " +
                        "retweet_orig_id bigint, " +
                        "retweet_orig_time bigint, " +
                        "retweets int" +
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
                        "firstname varchar(150), " +
                        "lastname varchar(150), " +
                        "photo varchar(200), " +
                        "birthday int, " +
                        "birthmonth int, " +
                        "birthyear int, " +
                        "name_r varchar(200)" +
                ")"
        );
    }

    public static void createDialogsTable(SQLiteDatabase db) {
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
}
