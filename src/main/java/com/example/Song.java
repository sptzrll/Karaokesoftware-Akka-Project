// Alla Spitzer 222114
// Olha Borysova 230606
// Anastasiia Kulyani 230612
// Dmytro Pahuba 230665

package com.example;

/*
    Klasse Song wird zur Verwaltung von vorgestellten Daten verwendet
 */
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