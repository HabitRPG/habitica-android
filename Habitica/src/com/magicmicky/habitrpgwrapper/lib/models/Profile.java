package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Created by MagicMicky on 16/03/14.
 */
public class Profile {
    private String name;
    private String blurb, imageUrl;

    public Profile(String name) {
        this(name, "","");
    }
    public Profile(String name, String blurb, String imageUrl) {
        this.name = name;
        this.blurb = blurb;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
