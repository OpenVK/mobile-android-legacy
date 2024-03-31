/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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

import java.util.ArrayList;

import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.legacy.databases.base.CacheDatabase;

public class MessagesCacheDB extends CacheDatabase {

    public static String prefix = "messages";

    public static ArrayList<Conversation> getConversationsList(Context ctx, int offset, int count) {
        Cursor cursor = null;
        ContentValues values = null;
        ArrayList<Conversation> result = new ArrayList<>();
        CacheOpenHelper helper = new CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            cursor = db.query("dialogs", null, null, null,
                    null, null, "time desc",
                    String.valueOf(offset) + "," + count);
            values = new ContentValues();
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    Conversation conv = new Conversation();
                    conv.peer_id = values.getAsLong("peer_id");
                    conv.title = values.getAsString("title");
                    result.add(conv);
                } while (cursor.moveToNext());
                cursor.close();
                db.close();
                helper.close();
                return result;
            }
            cursor.close();
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static int getConversationsCount(Context ctx) {
        int result = 0;
        CacheOpenHelper helper = new CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT count(*) FROM dialogs", null);
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static boolean containsMessage(Context ctx, int message_id) {
        boolean result = false;
        CacheOpenHelper helper = new CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cursor = db.query("messages", new String[]{"message_id"},
                    "message_id=" + message_id,
                    null, null, null, null);
            result = cursor.getCount() > 0;
            cursor.close();
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void deleteDialog(Context ctx, int peer) {
        CacheOpenHelper helper = new CacheOpenHelper(
                ctx.getApplicationContext(),
                getCurrentDatabaseName(ctx, prefix)
        );
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.delete("messages", "peer=" + peer, null);
            db.close();
            helper.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
