package com.trevor.ultimatehue;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.trevor.ultimatehue.factory.MediaFactory;
import com.trevor.ultimatehue.helpers.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumListActivity extends Activity {

    private static final String TAG = AlbumListActivity.class.toString();

    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        Log.i(TAG, "OnCreate");

        listView = (ListView) findViewById(R.id.albumList);
        Log.i(TAG, "Getting Album Info");

        Cursor cursor = MediaFactory.getAlbumcursor(this);

        // Columns we want to look for
        String [] columns = new String[] {MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST};

        // XML Views to bind the data to
        int [] xmlBind = new int[] {R.id.textViewItemOne, R.id.textViewItemTwo};

        // Create adapter with the above information set
        final SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this , R.layout.playlist_item, cursor , columns, xmlBind , 0);

        listView.setAdapter(dataAdapter);

        // listening to single listitem click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Album selected,  loading all songs");

                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                Intent intent = new Intent(view.getContext(), SongListActivity.class);
                intent.putExtra(Constants.ALBUM_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                startActivityForResult(intent, Constants.SONG_LIST_ACTIVITY);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Constants.SONG_LIST_ACTIVITY){

            Intent intent = new Intent();
            intent.putExtra(Constants.SONG_ID, data.getExtras().getInt(Constants.SONG_ID));
            setResult(Constants.ALBUM_LIST_ACTIVITY, intent);
            finish();
        }

    }
}
