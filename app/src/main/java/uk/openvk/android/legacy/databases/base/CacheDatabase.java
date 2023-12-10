package uk.openvk.android.legacy.databases.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.databases.CacheDatabaseTables;

public class CacheDatabase {
    public static String prefix = "";

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String db_name) {
            super(ctx, db_name, null, 1);
        }

        public CacheOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            switch (prefix) {
                case "audio":
                    CacheDatabaseTables.createAudioTracksTable(database);
                    break;
                case "wall":
                case "newsfeed":
                    CacheDatabaseTables.createWallPostTables(database);
                    break;
                default:
                    CacheDatabaseTables.createMainCacheTables(database);
                    break;
            }
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

    public static File getCurrentDatabasePath(Context ctx, String prefix) {
        OvkApplication app = (OvkApplication) ctx.getApplicationContext();
        String instance = app.getCurrentInstance();
        long user_id = app.getCurrentUserId();
        return ctx.getDatabasePath(String.format("%s_%s_a%s.db", prefix, instance, user_id));
    }

    public static String getCurrentDatabaseName(Context ctx, String prefix) {
        OvkApplication app = (OvkApplication) ctx.getApplicationContext();
        String instance = app.getCurrentInstance();
        long user_id = app.getCurrentUserId();
        return String.format("%s_%s_a%s.db", prefix, instance, user_id);
    }
}
