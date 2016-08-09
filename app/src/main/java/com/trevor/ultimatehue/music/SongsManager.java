package com.trevor.ultimatehue.music;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class SongsManager {

	private static final String TAG = SongsManager.class.toString();
	
	// Constructor
	public SongsManager(){
		
	}
	
	// Get all Album info
	public ArrayList<HashMap<String, String>> getAlbumcursor(Context context)
	{
		ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

        String where = null;
        ContentResolver cr = context.getContentResolver();
        final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        final String _id = MediaStore.Audio.Albums._ID; 
        final String album_id = MediaStore.Audio.Albums.ALBUM_ID; 
        final String album_name =MediaStore.Audio.Albums.ALBUM;
        final String artist = MediaStore.Audio.Albums.ARTIST;
        final String[]columns={_id,album_name, artist};
        Cursor cursor = cr.query(uri,columns,where,null, null);
        
        if(cursor != null) {
			Log.d(TAG, "SongsList is not Empty");
			while(cursor.moveToNext()) {
				//Song song = new Song();
				
				String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				String artist1 = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

				HashMap<String, String> album = new HashMap<String, String>();
                album.put("albumTitle", title);
                album.put("artist", artist1);
				
				songsList.add(album);
			}
		} else
			Log.w(TAG, "SongsList is Empty");
        
        return songsList;
	        
	}

	// Get all Track info
	public ArrayList<HashMap<String, String>> getTrackcursor(Context context)
	{
		ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
		
        String where = null;
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
	    
	    Cursor cursor = cr.query(uri,columns,null,null,null);
        
        if(cursor != null) {
			Log.d(TAG, "SongsList is not Empty");
			while(cursor.moveToNext()) {
				//Song song = new Song();
				
				String songTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
				String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				
				HashMap<String, String> song = new HashMap<String, String>();
				song.put("songTitle", songTitle);
				song.put("songPath", songPath);
				
				songsList.add(song);
			}
		} else
			Log.w(TAG, "SongsList is Empty");
        
        return songsList;
	}
	
	// Get all Playlists
	public  Cursor getPlaylistcursor(Context context)
	 {
	     ContentResolver resolver = context.getContentResolver();
	     final Uri uri=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
	     final String id = MediaStore.Audio.Playlists._ID;
	     final String name = MediaStore.Audio.Playlists.NAME;
	     final String[]columns = {id,name};
	     final String criteria = MediaStore.Audio.Playlists.NAME.length() + " > 0 " ;
	     final Cursor crplaylists = resolver.query(uri, columns, criteria, null,name + " ASC");
	     return crplaylists;
	 }

}
