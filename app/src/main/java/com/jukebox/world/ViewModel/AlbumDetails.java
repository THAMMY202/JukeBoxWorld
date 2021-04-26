package com.jukebox.world.ViewModel;

import java.util.Objects;

public class AlbumDetails {
    private String coverImageUrl;
    private String title;
    private String realiseDate;
    private String genre;
    private String price;
    private String artist;
    private Boolean isPublished;

    public AlbumDetails(){}

    public AlbumDetails(String coverImageUrl, String title, String realiseDate, String genre, String price,String artist,Boolean isPublished) {
        this.coverImageUrl = coverImageUrl;
        this.title = title;
        this.realiseDate = realiseDate;
        this.genre = genre;
        this.price = price;
        this.artist = artist;
        this.isPublished = isPublished;
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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Boolean getPublished() {
        return isPublished;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbumDetails)) return false;
        AlbumDetails that = (AlbumDetails) o;
        return Objects.equals(getCoverImageUrl(), that.getCoverImageUrl()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getRealiseDate(), that.getRealiseDate()) &&
                Objects.equals(getGenre(), that.getGenre()) &&
                Objects.equals(getPrice(), that.getPrice())  &&
                Objects.equals(getPublished(), that.getPublished())  &&
                Objects.equals(getArtist(), that.getArtist());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoverImageUrl(), getTitle(), getRealiseDate(), getGenre(),getPrice());
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
