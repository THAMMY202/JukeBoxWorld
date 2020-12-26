package com.jukebox.world.ViewModel;

public class SocialModel {
    private String name;
    private String url;
    private String key;

    public SocialModel(){}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SocialModel(String key, String name, String url) {
        this.key = key;
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
