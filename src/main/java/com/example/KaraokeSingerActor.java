package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.Random;


public class KaraokeSingerActor extends AbstractBehavior<KaraokeSingerActor.Message> {

    private final Random random;
    private final ActorRef<LibraryActor.Message> library;
    private final ActorRef<QueueManagerActor.Message> queueManager;
    private static ActorRef<PlaybackClientActor.Message> playback;
    private final int singerNumber;

    public interface Message { }

    //liefert Liste mit allen Artists
    public record ArtistsMessage(ArrayList<String> artistsList) implements Message, LibraryActor.Message { }
    //liefert Liste aller Songs eines Artists
    public record SongsMessage(ArrayList<String> songsList) implements Message { }
    //Singer kann anfangen zu singen
    public record StartSingingMessage(String artistName, String title,int duration) implements Message { }

    public static Behavior<Message> create(ActorRef<QueueManagerActor.Message> queueManager, ActorRef<LibraryActor.Message> library, ActorRef<PlaybackClientActor.Message> playback, int number) {
        return Behaviors.setup(context -> new KaraokeSingerActor(context, queueManager, library, playback, number) );
    }

    private KaraokeSingerActor(
            ActorContext<Message> context,
            ActorRef<QueueManagerActor.Message> queueManagerActor,
            ActorRef<LibraryActor.Message> library,
            ActorRef<PlaybackClientActor.Message> playback,
            int singerNumber) {
        super(context);
        this.random = new Random();
        this.queueManager = queueManagerActor;
        this.library = library;
        this.playback = playback;
        this.singerNumber = singerNumber;

        //this.library.tell(new LibraryActor.ListArtistsMessage());
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ArtistsMessage.class, this::onArtistsMessage)
                .onMessage(SongsMessage.class, this::onSongsMessage)
                .onMessage(StartSingingMessage.class, this::onStartSingingMessage)
                .build();
    }

    private Behavior<Message> onArtistsMessage(ArtistsMessage msg) {
        int randomArtistIndex = random.nextInt(((msg.artistsList.size())));
        library.tell(new LibraryActor.GetSongsMessage(this.getContext().getSelf(), msg.artistsList.get(randomArtistIndex), singerNumber));
        this.getContext().getLog().info(String.format("Singer %d chose %s", singerNumber, msg.artistsList.get(randomArtistIndex)));
        return this;
    }
    private Behavior<Message> onSongsMessage(SongsMessage msg) {
        int randomSongIndex = random.nextInt(msg.songsList.size());
        queueManager.tell(new QueueManagerActor.AddMessage(this.getContext().getSelf(), msg.songsList.get(randomSongIndex), singerNumber));
        this.getContext().getLog().info(String.format("Singer %d chose '%s'", singerNumber, msg.songsList.get(randomSongIndex)));
        return this;
    }
    private Behavior<Message> onStartSingingMessage(StartSingingMessage msg) {
        try {
            this.getContext().getLog().info(String.format("Singer %d starts singing: %s - '%s' for %d seconds", singerNumber, msg.artistName, msg.title,msg.duration));
            Thread.sleep(msg.duration);
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

}