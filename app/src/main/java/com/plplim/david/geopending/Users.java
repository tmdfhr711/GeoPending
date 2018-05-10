package com.plplim.david.geopending;

/**
 * Created by OHRok on 2018-04-19.
 */

public class Users extends UserId{
    String image;
    String name;
    String group;
    String token;

    public Users(){

    }
    public Users(String image, String name, String group, String token) {
        this.image = image;
        this.name = name;
        this.group = group;
        this.token = token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
