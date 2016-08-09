package com.trevor.ultimatehue.factory;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by nemo on 10/12/15.
 */
public class MediaFactory {

    private static final String TAG = MediaFactory.class.toString();

    // Get all Album info
    public static Cursor getAlbumcursor(Context context)
    {
        Log.d(TAG, "getAlbumcursor");
        ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

        String where = null;
        ContentResolver cr = context.getContentResolver();
        final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Albums._ID;
        //final String album_id = MediaStore.Audio.Albums.ALBUM_ID;
        final String album_name =MediaStore.Audio.Albums.ALBUM;
        final String artist = MediaStore.Audio.Albums.ARTIST;
        final String[]columns={_id, album_name, artist};
        return cr.query(uri,columns,where,null, artist);
    }

    public static Cursor getAllSongsForAlbumCursor(Context context, String albumName) {
        Log.d(TAG, "getAllSongsForAlbumCursor");

        String where = MediaStore.Audio.Media.ALBUM + " = ?";
        String [] whereVal = new String [] {albumName};
        final String track_id = MediaStore.Audio.Media._ID;
        final String track_no =MediaStore.Audio.Media.TRACK;
        final String track_name =MediaStore.Audio.Media.TITLE;
        final String artist = MediaStore.Audio.Media.ARTIST;
        final String duration = MediaStore.Audio.Media.DURATION;
        final String album = MediaStore.Audio.Media.ALBUM;
        final String composer = MediaStore.Audio.Media.COMPOSER;
        final String year = MediaStore.Audio.Media.YEAR;
        final String path = MediaStore.Audio.Media.DATA;
        final String[]columns={track_id, track_no, artist, track_name,album, duration, path, year, composer};

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr =  context.getContentResolver();

        return cr.query(uri,columns,where,whereVal,track_no);
    }

    public static Cursor getSongCursor(Context context, int songId) {
        Log.d(TAG, "getSongCursor ( " + songId + ")");

        String where = MediaStore.Audio.Media._ID + " = ?";
        String [] whereVal = new String [] {String.valueOf(songId)};
        final String track_id = MediaStore.Audio.Media._ID;
        final String track_name =MediaStore.Audio.Media.TITLE;
        final String path = MediaStore.Audio.Media.DATA;
        final String[]columns={track_id, track_name, path};

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr =  context.getContentResolver();

        return cr.query(uri,columns,where,whereVal,null);
    }

    public static int getAllSongsCount(Context context) {
        Log.d(TAG, "getAllSongsCount");


        final String track_id = MediaStore.Audio.Media._ID;
        final String track_name =MediaStore.Audio.Media.TITLE;
        final String path = MediaStore.Audio.Media.DATA;
        final String[]columns={track_id, track_name, path};

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr =  context.getContentResolver();

        Cursor cursor =  cr.query(uri, columns, null, null, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public static Cursor getAllSongsCursor(Context context) {
        Log.d(TAG, "getAllSongsCursor");


        final String track_id = MediaStore.Audio.Media._ID;
        final String track_no =MediaStore.Audio.Media.TRACK;
        final String track_name =MediaStore.Audio.Media.TITLE;
        final String artist = MediaStore.Audio.Media.ARTIST;
        final String duration = MediaStore.Audio.Media.DURATION;
        final String album = MediaStore.Audio.Media.ALBUM;
        final String composer = MediaStore.Audio.Media.COMPOSER;
        final String year = MediaStore.Audio.Media.YEAR;
        final String path = MediaStore.Audio.Media.DATA;
        final String[]columns={track_id, track_no, track_name, artist, duration, album, composer, year, path};

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr =  context.getContentResolver();

        return cr.query(uri, columns, null, null, null);
    }

    public static int getMaxSongId(Context context) {
        Log.d(TAG, "getMaxSongId");

        int max = 0;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String track_id = MediaStore.Audio.Media._ID;
        final String[]columns={"MAX(" + track_id + ")"};

        ContentResolver cr =  context.getContentResolver();

        Cursor cursor =  cr.query(uri, columns, null, null, null);

        if (cursor.moveToFirst()) {
            Log.i(TAG, "MAX IS " + cursor.getInt(0));
            max = cursor.getInt(0);
        }

        cursor.close();
        return max;
    }

    public static int getMinSongId(Context context) {
        Log.d(TAG, "getMinSongId");

        int min = 0;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String track_id = MediaStore.Audio.Media._ID;
        final String[]columns={"MIN(" + track_id + ")"};

        ContentResolver cr =  context.getContentResolver();

        Cursor cursor =  cr.query(uri, columns, null, null, null);

        if (cursor.moveToFirst()) {
            Log.i(TAG, "MIN IS " + cursor.getInt(0));
            min = cursor.getInt(0);
        }

        cursor.close();
        return min;
    }

    public static int getRandomSongId(Context context) {
        Log.d(TAG, "getRandomSongId");

        final String track_id = MediaStore.Audio.Media._ID;
        final String track_name =MediaStore.Audio.Media.TITLE;
        final String path = MediaStore.Audio.Media.DATA;
        final String[]columns={track_id, track_name, path};

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr =  context.getContentResolver();

        Cursor cursor = cr.query(uri, columns, null, null, null);

        Random rand = new Random();
        int rando = rand.nextInt((cursor.getCount() - 1) - 0 + 1) + 0;
        int songId;
        if(rando > 0) {
            cursor.moveToPosition(rando);
            songId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        } else {
            songId = 0;
        }

        cursor.close();
        return songId;

    }
}
