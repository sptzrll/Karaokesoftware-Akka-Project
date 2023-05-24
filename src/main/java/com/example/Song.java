package com.example;

public class Song {

    private final String artistName;
    private final String title;
    private final int duration;

    public Song(String name, String title, int duration){
        this.artistName = name;
        this.title = title;
        this.duration = duration;
    }

    public String getArtistName() {
        return artistName;
    }
    public String getTitle() {
        return title;
    }
    public int getDuration() {
        return duration;
    }
}