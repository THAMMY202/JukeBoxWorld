package com.jukebox.world.ViewModel;

import java.util.Objects;

public class AlbumDetails {
    private String coverImageUrl;
    private String title;
    private String realiseDate;
    private String genre;

    public AlbumDetails(){}

    public AlbumDetails(String coverImageUrl, String title, String realiseDate,String genre) {
        this.coverImageUrl = coverImageUrl;
        this.title = title;
        this.realiseDate = realiseDate;
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "AlbumDetails{" +
                "coverImageUrl='" + coverImageUrl + '\'' +
                ", title='" + title + '\'' +
                ", realiseDate='" + realiseDate + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbumDetails)) return false;
        AlbumDetails that = (AlbumDetails) o;
        return Objects.equals(getCoverImageUrl(), that.getCoverImageUrl()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getRealiseDate(), that.getRealiseDate()) &&
                Objects.equals(getGenre(), that.getGenre());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoverImageUrl(), getTitle(), getRealiseDate(), getGenre());
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRealiseDate() {
        return realiseDate;
    }

    public void setRealiseDate(String realiseDate) {
        this.realiseDate = realiseDate;
    }

}
