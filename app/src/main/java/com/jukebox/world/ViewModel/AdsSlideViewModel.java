package com.jukebox.world.ViewModel;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class AdsSlideViewModel {
    private String imageUrl, name;

    public AdsSlideViewModel() {
    }

    public AdsSlideViewModel(String imageUrl, String name) {
        this.imageUrl = imageUrl;
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("imageUrl", imageUrl);
        result.put("name", name);
        return result;
    }
}
