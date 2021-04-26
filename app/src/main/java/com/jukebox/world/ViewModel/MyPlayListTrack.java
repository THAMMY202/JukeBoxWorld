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

    public MyPlayListTrack(){}

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
                '}';
    }

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

    public String getDuration() {
        return trackDuration;
    }

    public void setDuration(String duration) {
        this.trackDuration = duration;
    }

    public MyPlayListTrack(String artist, String album, String title, String url, String cover, String type, String feature, boolean selected, String duration) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.url = url;
        this.cover = cover;
        this.type = type;
        this.feature = feature;
        this.trackDuration = duration;
        this.setSelected(selected);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
