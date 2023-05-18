package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.List;

public class LibraryActor extends AbstractBehavior<LibraryActor.Message> {

    private List<Song> songs;


    public interface Message {
    }

    public static class GetSongsMessage implements Message {
        public final String name;

        public GetSongsMessage(String name) {
            this.name = name;
        }
    }

    public static class ListArtistsMessage implements Message {
    }


    public record CreateMessage(ActorRef<AkkaMainSystem.Create> someReference) implements Message {
    }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> new LibraryActor(context));
    }


    private LibraryActor(ActorContext<Message> context) {
        super(context);
        //мы переделаем на свое потом на json
//        songs = new ArrayList<Song>();

        songs = new ArrayList<>();
        songs.add(new Song("Artist1", "Song1", 8));
        songs.add(new Song("Artist1", "Song2", 6));
        songs.add(new Song("Artist2", "Song3", 10));
    }


    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetSongsMessage.class, this::onGetSongsMessage)
                .onMessage(ListArtistsMessage.class, this::onListArtistsMessage)
                .build();
    }


    private Behavior<Message> onGetSongsMessage(GetSongsMessage msg) {
        StringBuilder str = new StringBuilder("Artist " + msg.name + " wrote songs: ");
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getArtistName().equals(msg.name)){
                if (i == songs.size() - 1){
                    str.append(songs.get(i).getTitle()).append(".");
                }else{
                    str.append(songs.get(i).getTitle()).append(", ");
                }
            }
        }
        this.getContext().getLog().info("{}", str);
        return this;
    }

    private Behavior<Message> onListArtistsMessage(ListArtistsMessage msg) {
        StringBuilder str = new StringBuilder("Artists: ");
        for (int i = 0; i < songs.size(); i++) {
            if (i == songs.size() - 1){
                str.append(songs.get(i).getArtistName()).append(".");
            }else{
                str.append(songs.get(i).getArtistName()).append(", ");
            }
        }
        this.getContext().getLog().info("{}", str);
        return this;
    }
}
