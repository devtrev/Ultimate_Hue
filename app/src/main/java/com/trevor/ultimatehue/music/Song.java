package com.trevor.ultimatehue.music;


public class Song {

	private String _id; 
	private String album_id; 
	private String album_name;
	private String artist;
	private String title;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getAlbum_id() {
		return album_id;
	}
	public void setAlbum_id(String album_id) {
		this.album_id = album_id;
	}
	public String getAlbum_name() {
		return album_name;
	}
	public void setAlbum_name(String album_name) {
		this.album_name = album_name;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}


}
