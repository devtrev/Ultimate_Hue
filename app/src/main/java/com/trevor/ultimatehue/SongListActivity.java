package com.trevor.ultimatehue;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.trevor.ultimatehue.factory.MediaFactory;
import com.trevor.ultimatehue.helpers.Constants;

public class SongListActivity extends AppCompatActivity {
    private static final String TAG = SongListActivity.class.toString();

    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        Bundle extras = getIntent().getExtras();

        if (extras == null) {
            setResult(Constants.ERROR);
            finish();
        } else {
            String albumName = extras.getString(Constants.ALBUM_NAME);

            listView = (ListView) findViewById(R.id.songList);

            // Gets all songs for album
            Cursor cursor = MediaFactory.getAllSongsForAlbumCursor(this, albumName);

            // Columns we want to look for
            String[] columns = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};

            // XML Views to bind the data to
            int[] xmlBind = new int[]{R.id.textViewItemOne, R.id.textViewItemTwo};

            // Create adapter with the above information set
            final SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, R.layout.playlist_item, cursor, columns, xmlBind, 0);

            listView.setAdapter(dataAdapter);

            // listening to single listitem click
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "Song selected, passing back to be played");

                    Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                    Intent intent = new Intent();
                    intent.putExtra(Constants.SONG_ID, cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                    setResult(Constants.SONG_LIST_ACTIVITY, intent);
                    finish();
                }
            });
        }
    }


}
