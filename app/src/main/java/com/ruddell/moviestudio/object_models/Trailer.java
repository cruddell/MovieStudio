package com.ruddell.moviestudio.object_models;

/**
 * Created by chris on 12/10/15.
 */
public class Trailer {
    public String id;
    public String key;
    public String name;
    public String site;

    public Trailer(String id, String key, String name, String site) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.site = site;
    }
}
