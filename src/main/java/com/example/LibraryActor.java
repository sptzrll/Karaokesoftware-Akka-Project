package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.List;

public class LibraryActor extends AbstractBehavior<LibraryActor.Message> {

    private final List<Song> songs;

    public interface Message { }

    public record Start(ActorRef<AkkaMainSystem.Create> akkaMainSystem, ActorRef<PlaybackClientActor.Message> playback) implements Message {}
    public record GetSongsMessage(ActorRef<KaraokeSingerActor.Message> singer, String artistName, int singerNumber) implements Message, KaraokeSingerActor.Message { }
    public record ListArtistsMessage(ActorRef<KaraokeSingerActor.Message> singer, int singerNumber) implements Message{}

    public static Behavior<Message> create() {
        return Behaviors.setup(LibraryActor::new);
    }

    private LibraryActor(ActorContext<Message> context) {
        super(context);

        songs = new ArrayList<>();
        songs.add(new Song("Drake", "Forever", 12));
        songs.add(new Song("Drake", "Headlines", 7));
        songs.add(new Song("Drake", "Best I Ever Had", 9));

        songs.add(new Song("Bad Bunny", "Diles", 10));
        songs.add(new Song("Bad Bunny", "Soy Peor", 9));
        songs.add(new Song("Bad Bunny", "I Like It", 18));

        songs.add(new Song("Ed Sheeran", "Thinking Out Loud", 10));
        songs.add(new Song("Ed Sheeran", "Perfect", 9));
        songs.add(new Song("Ed Sheeran", "Photograph", 9));

        songs.add(new Song("Taylor Swift", "Shake it off", 7));
        songs.add(new Song("Taylor Swift", "Love Story", 8));
        songs.add(new Song("Taylor Swift", "Blank Space", 8));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Start.class, this::onStart)
                .onMessage(GetSongsMessage.class, this::onGetSongsMessage)
                .onMessage(ListArtistsMessage.class, this::onListArtistsMessage)
                .build();
    }

    private Behavior<Message> onStart(Start msg) {
        msg.playback.tell(new PlaybackClientActor.GetList(songs));
        return this;
    }

    private Behavior<Message> onListArtistsMessage(ListArtistsMessage msg) {
        ArrayList<String> artistsList = new ArrayList<>();

        for (Song song : songs) {
            if (!artistsList.contains(song.getArtistName())) {
                artistsList.add(song.getArtistName());
            }
        }

        msg.singer.tell(new KaraokeSingerActor.ArtistsMessage(artistsList));
        this.getContext().getLog().info(String.format("Library sent artistList to Singer %d: %s", msg.singerNumber, artistsList));
        return this;
    }

    private Behavior<Message> onGetSongsMessage(GetSongsMessage msg) {
        ArrayList<String> songsList = new ArrayList<>();

        for (Song song: songs) {
            if (song.getArtistName().equals(msg.artistName)) {
                songsList.add(song.getTitle());
            }
        }
        msg.singer.tell(new KaraokeSingerActor.SongsMessage(songsList));
        this.getContext().getLog().info(String.format("Library sent songsList to Singer :%s", songsList));
        return this;
    }
}