package uk.openvk.android.legacy.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import uk.openvk.android.legacy.OvkApplication;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

@SuppressLint("Recycle")
public class RealPathUtil {
    public static String getRealPathFromURI(Context context, Uri uri) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String filePath = "";
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String wholeID = DocumentsContract.getDocumentId(uri);
                // Split at colon, use second item in the array
                Log.d(OvkApplication.APP_TAG, wholeID);
                String[] splits = wholeID.split(":");
                if (splits.length == 2) {
                    String id = splits[1];
                    if(splits[0].equals("primary")) {
                        return Environment.getExternalStorageDirectory() + "/" + id;
                    } else {
                        String[] column = {MediaStore.Images.Media.DATA};
                        // where id is equal to
                        String sel = MediaStore.Images.Media._ID + "=?";
                        Cursor cursor = context.getContentResolver()
                                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        column, sel, new String[]{id}, null);
                        assert cursor != null;
                        int columnIndex = cursor.getColumnIndex(column[0]);
                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(columnIndex);
                        }
                        cursor.close();
                    }
                }
            } else {
                filePath = uri.getPath();
            }
            return filePath;
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            String[] proj = { MediaStore.Images.Media.DATA };
            String result = null;
            CursorLoader cursorLoader = new CursorLoader(context, uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);
            }
            return result;
        } else {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }
}
