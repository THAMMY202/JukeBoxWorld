package com.jukebox.world.ViewModel;

import java.util.Objects;

public class Track {
    private String title;
    private String url;
    private String type;
    private String feature;

    public Track(){

    }

    public Track(String title, String url, String type, String feature) {
        this.title = title;
        this.url = url;
        this.type = type;
        this.feature = feature;
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

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(title, track.title) &&
                Objects.equals(url, track.url) &&
                Objects.equals(type, track.type) &&
                Objects.equals(feature, track.feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, url, type, feature);
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
}
