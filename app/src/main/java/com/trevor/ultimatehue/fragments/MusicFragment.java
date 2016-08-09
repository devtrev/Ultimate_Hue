package com.trevor.ultimatehue.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLightState;
import com.trevor.ultimatehue.MainActivity;
import com.trevor.ultimatehue.AlbumListActivity;
import com.trevor.ultimatehue.NewGroupActivity;
import com.trevor.ultimatehue.R;
import com.trevor.ultimatehue.factory.MediaFactory;
import com.trevor.ultimatehue.factory.SettingsFactory;
import com.trevor.ultimatehue.helpers.AnalyticsHelper;
import com.trevor.ultimatehue.helpers.Constants;
import com.trevor.ultimatehue.helpers.DatabaseHelper;
import com.trevor.ultimatehue.lights.LightGroup;
import com.trevor.ultimatehue.music.SongsManager;
import com.trevor.ultimatehue.music.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MusicFragment extends Fragment implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MusicFragment.class.toString();
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final int NEW_GROUP_ACTIVITY = 151;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_MEMORY = 1;
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    public static final int MY_PERMISSIONS_REQUEST_MODIFY_AUDIO_SETTINGS = 3;

    private PHHueSDK phHueSDK;
    private PHBridge bridge;

    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private Spinner spnLightGroups;
    private SeekBar seekBarMusicSaturation;
    private SeekBar seekBarMusicBrightness;
    private CheckBox quickTransitions;
    //private TextView txtRefreshRate;
    //private SeekBar seekBarRefreshRate;

    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    ;
    private Utilities utils;
    //private int seekForwardTime = 5000; // 5000 milliseconds
    //private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    //private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    private Visualizer visual;
    private boolean isFirstPlay;

    private List<LightGroup> allGroups;
    int position = 0;
    private SQLiteDatabase db;
    private int numberOfSongsPLayed;
    private View rootView;
    //private int refreshRate;

    //private Equalizer mEqualizer;

    public static MusicFragment newInstance(int sectionNumber) {
        Log.i(TAG, "newInstance");

        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MusicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "MusicFragment : OnCreate");

        // This is used in analytics to track how many songs are played for each session
        numberOfSongsPLayed = 0;

        rootView = inflater.inflate(R.layout.fragment_music, container, false);

        findViewsById();

        if(checkPermissions())
            populateView();

        showFirstTimeHelpMessage();

        // Record Screen Load
        AnalyticsHelper.analyticsScreenCapture((AnalyticsHelper) getActivity().getApplication(), getClass().getSimpleName());

        // Finished the View creation
        return rootView;
    }

    private void findViewsById() {
        Log.d(TAG, "findViewsById");
        // All player buttons
        btnPlay = (ImageButton) rootView.findViewById(R.id.btnPlay);
        //btnForward = (ImageButton) rootView.findViewById(R.id.btnForward);
        //btnBackward = (ImageButton) rootView.findViewById(R.id.btnBackward);
        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) rootView.findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) rootView.findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) rootView.findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) rootView.findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) rootView.findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) rootView.findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) rootView.findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) rootView.findViewById(R.id.songTotalDurationLabel);
        spnLightGroups = (Spinner) rootView.findViewById(R.id.spnMusicLightGroups);
        seekBarMusicSaturation = (SeekBar) rootView.findViewById(R.id.seekBarMusicSaturation);
        seekBarMusicBrightness = (SeekBar) rootView.findViewById(R.id.seekBarMusicBrightness);
        quickTransitions = (CheckBox) rootView.findViewById(R.id.quickTransitions);
        //txtRefreshRate = (TextView) rootView.findViewById(R.id.txtRefreshRate);
        //seekBarRefreshRate = (SeekBar) rootView.findViewById(R.id.seekBarRefreshRate);
    }

    private void populateView() {
        // Get HUE SDK
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();

        // Add light Groups
        Log.d(TAG, "Setting up Spinner for Groups");
        List<PHGroup> phGroups = bridge.getResourceCache().getAllGroups();

        allGroups = new ArrayList<>();
        for (PHGroup phGroup : phGroups) {
            allGroups.add(new LightGroup(phGroup));
        }

        if(allGroups.size() > 0) {
            allGroups = sort(allGroups);

            Log.d(TAG, "There are " + allGroups.size() + " groups");
            String[] groupStringArray = new String[allGroups.size()];
            int i = 0;
            for (final LightGroup group : allGroups) {
                groupStringArray[i] = group.getPhGroup().getName();
                i++;
            }

            openDatabase();

            int lastGroup = Integer.parseInt(SettingsFactory.getSetting(db, Constants.SETTING_LAST_GROUP));
            if(lastGroup >= allGroups.size())
                lastGroup = 0;

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, groupStringArray);
            spnLightGroups.setAdapter(adapter);
            spnLightGroups.setSelection(lastGroup);

            // Mediaplayer
            mp = new MediaPlayer();
            utils = new Utilities();

            // Listeners
            songProgressBar.setOnSeekBarChangeListener(this); // Important
            mp.setOnCompletionListener(this); // Important

            currentSongIndex = 0;
            isFirstPlay = true;

            seekBarMusicBrightness.setProgress(Integer.parseInt(SettingsFactory.getSetting(db, Constants.SETTING_MUSIC_BRIGHTNESS)));
            seekBarMusicSaturation.setProgress(Integer.parseInt(SettingsFactory.getSetting(db, Constants.SETTING_MUSIC_SATURATION)));
            quickTransitions.setChecked(Boolean.parseBoolean(SettingsFactory.getSetting(db, Constants.SETTING_MUSIC_TRANSITION)));

            currentSongIndex = Integer.parseInt(SettingsFactory.getSetting(db, Constants.SETTING_LAST_SONG_PLAYED));

            // If it is -1 then it was never set and this is users first time in the app
            if(currentSongIndex == -1) {
                currentSongIndex = MediaFactory.getMinSongId(getContext());
                SettingsFactory.updateSetting(db, Constants.SETTING_LAST_SONG_PLAYED, String.valueOf(currentSongIndex));
            }

            closeDatabase();

            Cursor cursor = MediaFactory.getSongCursor(getActivity(), currentSongIndex);

            if (cursor.moveToFirst()) {
                String songTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                //String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                cursor.close();
                songTitleLabel.setText(songTitle);

            /*
            try {
                mp.setDataSource(songPath);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            }

            if (!cursor.isClosed())
                cursor.close();


            /**
             * Play button click event
             * plays a song and changes button to pause image
             * pauses a song and changes button to play image
             * */
            btnPlay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    try {
                        // check for already playing
                        if (mp.isPlaying()) {
                            if (mp != null) {
                                mp.pause();

                                // Changing button image to play button
                                btnPlay.setImageResource(R.drawable.btn_play);

                                if (visual != null)
                                    visual.setEnabled(false);
                            }
                        } else {
                            position = spnLightGroups.getSelectedItemPosition();

                            // Resume song
                            if (mp != null) {
                                mp.start();

                                // Changing button image to pause button
                                btnPlay.setImageResource(R.drawable.btn_pause);

                                // Allow capturing music for light output
                                visual.setEnabled(true);

                                if (isFirstPlay) {
                                    Log.w(TAG, "First Play");

                                    playSong(currentSongIndex);

                                /*
                                // set Progress bar values
                                songProgressBar.setProgress(0);
                                songProgressBar.setMax(100);

                                // Updating progress bar
                                updateProgressBar();

                                isFirstPlay = false;*/
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }

                }
            });

            /**
             * Next button click event
             * Plays next song by taking currentSongIndex + 1
             * */
            btnNext.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    playNextSong();
                }
            });

            /**
             * Back button click event
             * Plays previous song by currentSongIndex - 1
             * */
            btnPrevious.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    playPreviousSong();
                }
            });

            /**
             * Button Click event for Repeat button
             * Enables repeat flag to true
             * */
            btnRepeat.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (isRepeat) {
                        isRepeat = false;
                        Toast.makeText(getActivity().getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                        btnRepeat.setImageResource(R.drawable.btn_repeat);
                    } else {
                        // make repeat to true
                        isRepeat = true;
                        Toast.makeText(getActivity().getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                        // make shuffle to false
                        isShuffle = false;
                        btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                        btnShuffle.setImageResource(R.drawable.btn_shuffle);
                    }
                }
            });

            /**
             * Button Click event for Shuffle button
             * Enables shuffle flag to true
             * */
            btnShuffle.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (isShuffle) {
                        isShuffle = false;
                        Toast.makeText(getActivity().getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                        btnShuffle.setImageResource(R.drawable.btn_shuffle);
                    } else {
                        // make repeat to true
                        isShuffle = true;
                        Toast.makeText(getActivity().getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                        // make shuffle to false
                        isRepeat = false;
                        btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                        btnRepeat.setImageResource(R.drawable.btn_repeat);
                    }
                }
            });

            /**
             * Button Click event for Play list click event
             * Launches list activity which displays list of songs
             * */
            btnPlaylist.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(getActivity().getApplicationContext(), AlbumListActivity.class);
                    startActivityForResult(i, Constants.ALBUM_LIST_ACTIVITY);
                }
            });

            seekBarMusicBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    openDatabase();
                    SettingsFactory.updateSetting(db, Constants.SETTING_MUSIC_BRIGHTNESS, String.valueOf(seekBarMusicBrightness.getProgress()));
                    closeDatabase();
                }
            });

            seekBarMusicSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    openDatabase();
                    SettingsFactory.updateSetting(db, Constants.SETTING_MUSIC_SATURATION, String.valueOf(seekBarMusicSaturation.getProgress()));
                    closeDatabase();
                }
            });

            quickTransitions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    openDatabase();
                    SettingsFactory.updateSetting(db, Constants.SETTING_MUSIC_TRANSITION, String.valueOf(isChecked));
                    closeDatabase();
                }
            });

            setupVisulaizer();
        } else {
            Log.w(TAG, "No groups exist - asking user to create new");

            showCreateGroupDialog();
        }
    }

    private boolean checkPermissions() {

        boolean allPermissionsGood = true;

        // Check to see if Permissions exist for reading external storage
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsGood = false;

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_MEMORY);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // Check to see if Permissions exist for Recording Audio
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsGood = false;

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // Check to see if Permissions exist for modifying audio settings
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsGood = false;

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.MODIFY_AUDIO_SETTINGS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS},
                        MY_PERMISSIONS_REQUEST_MODIFY_AUDIO_SETTINGS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        return  allPermissionsGood;
    }

    private void updateLights(int hueColor) {
        Log.d(TAG, "Updating the lights to new Hue : " + hueColor);
        PHLightState state = new PHLightState();
        state.setHue(hueColor);
        state.setSaturation(seekBarMusicSaturation.getProgress());
        state.setBrightness(seekBarMusicBrightness.getProgress());

        if (quickTransitions.isChecked())
            state.setTransitionTime(0);

        if (allGroups.get(position) != null) {
            String lightGroupIdentifier = allGroups.get(position).getPhGroup().getIdentifier();
            bridge.setLightStateForGroup(lightGroupIdentifier, state);
        } else {
            Log.e(TAG, "No group selected, updating all...");
            bridge.setLightStateForDefaultGroup(state);
        }
    }

    private void setupVisulaizer() {
        // Create the Visualizer object and attach it to our media player.
        try {
            Log.i(TAG, "Initializing Visulaizer");

            // this line is not actually causing Exception ,It is because u are
            // enabling the visulizer to capture data & and after that setting
            // the capture size of buffer. U can't make any changes after
            // enabling it. I tried this same code and it's working fine for me
            Log.d(TAG, "mediaPlayer Id " + mp.getAudioSessionId());
            visual = new Visualizer(mp.getAudioSessionId());

            visual.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            visual.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveForm,
                                                  int samplingRate) {
                    Log.i(TAG, "Capturing Data : " + waveForm.length);

                    float minWave = Float.parseFloat(Byte.toString(waveForm[0])) + 128;
                    float maxWave = Float.parseFloat(Byte.toString(waveForm[waveForm.length - 1])) + 128;
                    float centerWave = Float.parseFloat(Byte.toString(waveForm[waveForm.length / 2])) + 128;

                    float hueLights = (centerWave / 256) * 65535;
                    //huelights = huelights * 65535;

                    //Log.d(TAG, "Min WaveForm : " + minWave + " Hue Setting : " + (minWave / 256) * 65535);
                    //Log.d(TAG, "Max WaveForm : " + maxWave + " Hue Setting : " + (int)hueLights);
                    //Log.d(TAG, "Center WaveForm : " + centerWave + " Hue Setting : " + (int)hueLights);

                    if (visual.getEnabled())
                        updateLights((int) hueLights);
                }

                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    Log.i(TAG, "OnFftDataCapture - Gotta do something here");
                }
                // Set the capture rate 20 is about one second
            }, (Visualizer.getMaxCaptureRate() / 30), true, false);

        } catch (Exception e) {
            Log.e(TAG, "Error while handling Music " + e.toString());
            Log.e(TAG, e.getMessage());
            e.printStackTrace();

        }
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Constants.ALBUM_LIST_ACTIVITY) {
            Log.d(TAG, "Song returned, saving index");
            currentSongIndex = data.getExtras().getInt(Constants.SONG_ID);

            if(visual != null) {
                // Need to Set the visualized to be enabled
                visual.setEnabled(true);
            } else {
                populateView();
                visual.setEnabled(true);
            }

            // play selected song
            playSong(currentSongIndex);
        } else if (resultCode == NEW_GROUP_ACTIVITY) {
            Log.i(TAG, "Adding New Group Result Returned");

            Boolean success = data.getExtras().getBoolean(Constants.NEW_GROUP_ADDED);

            // If success was returned then a new Group was added and we need to refresh
            // THe group page
            if (success != null && success == true) {
                try {
                    Thread.sleep(300);
                    populateView();
                } catch (Exception e) {
                    Log.e(TAG, "Error repopulating view");
                    e.printStackTrace();
                }

            }

        }

    }

    /**
     * Function to play a song
     *
     * @param songIndex - index of song
     */
    public void playSong(int songIndex) {
        // Play song
        try {
            Log.d(TAG, "Play Song");

            isFirstPlay = false;

            Cursor cursor = MediaFactory.getSongCursor(getActivity(), songIndex);

            if (cursor.moveToFirst()) {
                String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String songTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

                //Log.d(TAG, songPath);
                //Log.d(TAG, songTitle);

                mp.reset();
                mp.setDataSource(songPath);
                mp.prepare();
                mp.start();

                position = spnLightGroups.getSelectedItemPosition();

                // Displaying Song title
                songTitleLabel.setText(songTitle);

                // Changing Button Image to pause image
                btnPlay.setImageResource(R.drawable.btn_pause);

                // set Progress bar values
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);

                // Updating progress bar
                updateProgressBar();

                openDatabase();
                SettingsFactory.updateSetting(db, Constants.SETTING_LAST_SONG_PLAYED, String.valueOf(songIndex));
                SettingsFactory.updateSetting(db, Constants.SETTING_LAST_GROUP, String.valueOf(position));
                closeDatabase();
            }

            cursor.close();

            AnalyticsHelper application = (AnalyticsHelper) getActivity().getApplication();
            String category = "Music Player";
            String action = "Play Song";
            String label = String.valueOf(++numberOfSongsPLayed);

            AnalyticsHelper.analyticsEvent(application, category, action, label);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {
        Log.i(TAG, "onCompletion");

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if (isShuffle) {
            Log.d(TAG, "Shuffling");
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = MediaFactory.getRandomSongId(getActivity());
            playSong(currentSongIndex);
        } else {
            // no repeat or shuffle ON - play next song
            playNextSong();
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void playNextSong() {
        Log.d(TAG, "playNextSong");

        boolean songFound = false;
        int max = MediaFactory.getMaxSongId(getActivity());

        for (int i = ++currentSongIndex; i < max; i++) {
            Cursor cursor = MediaFactory.getSongCursor(getActivity(), i);

            if (cursor.moveToFirst()) {
                songFound = true;
                currentSongIndex = i;
                cursor.close();
                break;
            }

            cursor.close();
        }

        if (songFound == false) {
            Log.w(TAG, "THERE WERE NO SONGS FOUND BEFORE END, SETTING TO FIRST SONG");

            // Set to the first song that was found
            currentSongIndex = MediaFactory.getMinSongId(getActivity());
        }

        playSong(currentSongIndex);
    }

    private void playPreviousSong() {
        Log.d(TAG, "playPreviousSong");

        boolean songFound = false;
        int min = MediaFactory.getMinSongId(getActivity());

        for (int i = --currentSongIndex; i > min; i--) {
            Cursor cursor = MediaFactory.getSongCursor(getActivity(), i);

            if (cursor.moveToFirst()) {
                songFound = true;
                currentSongIndex = i;
                cursor.close();
                break;
            }
            cursor.close();
        }

        if (songFound == false) {
            Log.w(TAG, "THERE WERE NO SONGS FOUND BEFORE BEGINNING, SETTING TO LAST SONG");

            // Set to the Last song that was found
            currentSongIndex = MediaFactory.getMaxSongId(getActivity());
        }

        playSong(currentSongIndex);
    }

    private List<LightGroup> sort(List<LightGroup> groupList) {
        Log.d(TAG, "sort()");
        int lightCount;
        int tempLightCount;

        // Sorts from most lights in group to the fewest
        for(int i = 0; i < groupList.size() - 1; i++) {
            lightCount = groupList.get(i).getPhGroup().getLightIdentifiers().size();
            tempLightCount = groupList.get(i + 1).getPhGroup().getLightIdentifiers().size();

            if (tempLightCount > lightCount) {
                Log.d(TAG, "Attempting to swap");
                LightGroup temp = groupList.get(i);
                groupList.remove(i);
                groupList.add(i + 1, temp);
            }

            for(int ii = 0; ii < groupList.size() - 1; ii++) {
                lightCount = groupList.get(ii).getPhGroup().getLightIdentifiers().size();
                tempLightCount = groupList.get(ii + 1).getPhGroup().getLightIdentifiers().size();

                if (tempLightCount > lightCount) {
                    Log.d(TAG, "Attempting to swap");
                    LightGroup temp = groupList.get(ii);
                    groupList.remove(ii);
                    groupList.add(ii + 1, temp);
                }

            }
        }

        return groupList;
    }

    private void showCreateGroupDialog() {
        Log.i(TAG, "showCreateGroupDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Warning - No Groups");
        alert.setMessage("There are currently no groups created. You will need to create a group before you can use this feature. You can either create one now or later by going to the Light Groups tab and creating a group there. ");

        alert.setPositiveButton("Create Group", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "User is creating new group - Load New Group Activity");

                Intent intent = new Intent(getActivity().getApplicationContext(), NewGroupActivity.class);
                intent.putExtra(Constants.IS_UPDATE, false);
                startActivityForResult(intent, NEW_GROUP_ACTIVITY);
            }
        });
        alert.setNegativeButton("Maybe Later", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "User chose not to create new group, do nothing.");
            }
        });

        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_MEMORY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    boolean allPerms = true;
                    // permission was granted, yay! Do the
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.MODIFY_AUDIO_SETTINGS)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }

                    if (allPerms) {
                        // All Permissions are granted, proceed
                        populateView();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO need to add popup to tell user this won't work until access granted
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    boolean allPerms = true;
                    // permission was granted, yay! Do the
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.MODIFY_AUDIO_SETTINGS)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }

                    if (allPerms) {
                        // All Permissions are granted, proceed
                        populateView();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO need to add popup to tell user this won't work until access granted
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_MODIFY_AUDIO_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    boolean allPerms = true;
                    // permission was granted, yay! Do the
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.MODIFY_AUDIO_SETTINGS)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                        allPerms = false;
                    }

                    if (allPerms) {
                        // All Permissions are granted, proceed
                        populateView();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO need to add popup to tell user this won't work until access granted
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void startMainActivity() {
        Log.d(TAG, "startMainActivity");
        Intent intent = new Intent(getContext(), com.philips.lighting.quickstart.PHHomeActivity.class);
        startActivity(intent);
    }

    private void showFirstTimeHelpMessage() {
        openDatabase();

        String showHelpMessage = SettingsFactory.getSetting(db, Constants.SETTING_MUSIC_HELP);
        if(showHelpMessage.equalsIgnoreCase("false") || showHelpMessage.equalsIgnoreCase("-1")) {
            showHelpDialog();

            SettingsFactory.updateOrInsert(db, Constants.SETTING_MUSIC_HELP, "true");
        }

        closeDatabase();
    }

    private void showHelpDialog() {
        Log.i(TAG, "showHelpDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Ultimate Hue");
        alert.setMessage("Update your lights to your favorite songs. In order to select a song click the 3 bars on the music player to open up all songs on your device. Then simply click play ");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    private void closeDatabase() {
        Log.i(TAG, "closeDatabase()");
        // Check if Database is closed, if not then close
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    private void openDatabase() {
        Log.i(TAG, "openDatabase()");
        // Check if Database is open, if not then open
        if (db == null || !db.isOpen()) {
            db = (DatabaseHelper.getInstance(getContext())).getWritableDatabase();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(db != null)
            db.releaseReference();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (phHueSDK == null) {
            phHueSDK = PHHueSDK.create();
        }

        if (phHueSDK.getSelectedBridge() == null) {
            Log.i(TAG, "No bridge currently selected, need to reinstantiate the bridge connection");

            startMainActivity();
        } else {
            bridge = phHueSDK.getSelectedBridge();
        }

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy Called");

        super.onDestroy();

        if(visual != null)
            visual.setEnabled(false);

        if(mp != null) {
            mp.stop();
            mp.release();
        }

        if(mHandler != null)
            mHandler.removeCallbacks(mUpdateTimeTask);

        if (bridge != null) {

            if (phHueSDK != null) {
                if (phHueSDK.isHeartbeatEnabled(bridge)) {
                    phHueSDK.disableHeartbeat(bridge);
                }

                phHueSDK.disconnect(bridge);
            }
        }
    }
}
