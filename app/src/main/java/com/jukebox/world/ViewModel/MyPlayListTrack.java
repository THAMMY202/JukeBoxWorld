package com.jukebox.world.ViewModel;

public class MyPlayListTrack {
    private String artist;
    private String album;
    private String title;
    private String url;
    private String cover;
    private String type;
    private String feature;
    private String trackDuration;
    private  boolean selected;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getTrackDuration() {
        return trackDuration;
    }

    public void setTrackDuration(String trackDuration) {
        this.trackDuration = trackDuration;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "MyPlayListTrack{" +
                "artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", cover='" + cover + '\'' +
                ", type='" + type + '\'' +
                ", feature='" + feature + '\'' +
                ", trackDuration='" + trackDuration + '\'' +
                ", selected=" + selected +
                '}';
    }

    public MyPlayListTrack(String artist, String album, String title, String url, String cover, String type, String feature, String trackDuration, boolean selected) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.url = url;
        this.cover = cover;
        this.type = type;
        this.feature = feature;
        this.trackDuration = trackDuration;
        this.selected = selected;
    }

    public MyPlayListTrack(){}

}
