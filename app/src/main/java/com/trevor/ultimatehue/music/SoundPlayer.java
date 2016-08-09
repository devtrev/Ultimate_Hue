package com.trevor.ultimatehue.music;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundPlayer {

	private static String TAG = SoundPlayer.class.toString();

	public static MediaPlayer mp;
	
	public static void playSound(final Context mContext, final int soundToPlay) {
		Thread thread = new Thread()
		{
		    @Override
		    public void run() {
		        try {
					Log.i(TAG, "Attempting to play sound");
                    mp = MediaPlayer.create(mContext, soundToPlay);
		    	    mp.start();

		    	    // Loop to do nothing while the sound is playing
		    	    while(mp.isPlaying()) {
					}
		    	    
		    	    // Stop the sound officially
		    		mp.stop();
		        } catch (Exception e) {
		        	Log.e(TAG, "Error while playing sound : " + e.toString());
		            e.printStackTrace();
		        }
		    }
		};

		thread.start();		
	}

    public static void stopSound() {
		if (mp != null && mp.isPlaying()) {
			//mp.pause();
			mp.stop();
		}
    }
}
