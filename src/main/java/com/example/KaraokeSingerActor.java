// Alla Spitzer 222114
// Olha Borysova 230606
// Anastasiia Kulyani 230612
// Dmytro Pahuba 230665

package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;


public class KaraokeSingerActor extends AbstractBehavior<KaraokeSingerActor.Message> {

    private final Random random;
    private final ActorRef<LibraryActor.Message> library;
    private final ActorRef<QueueManagerActor.Message> queueManager;
    private final TimerScheduler<KaraokeSingerActor.Message> timers;
    private final int singerNumber;

    public interface Message { }

    //liefert Liste mit allen Artists
    public record ArtistsMessage(ArrayList<String> artistsList) implements Message, LibraryActor.Message { }
    //liefert Liste aller Songs eines Artists
    public record SongsMessage(ArrayList<String> songsList) implements Message { }
    //Singer kann anfangen zu singen
    public record StartSingingMessage(String artistName, String title,int duration) implements Message { }
    public enum sendListArtistsMessage implements Message { INSTANCE }

    public static Behavior<KaraokeSingerActor.Message> create(ActorRef<QueueManagerActor.Message> queueManager, ActorRef<LibraryActor.Message> library, int singerNumber) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new KaraokeSingerActor(context, queueManager, library, timers, singerNumber)));
    }

    private KaraokeSingerActor(
            ActorContext<Message> context,
            ActorRef<QueueManagerActor.Message> queueManagerActor,
            ActorRef<LibraryActor.Message> library,
            TimerScheduler<KaraokeSingerActor.Message> timers,
            int singerNumber) {
        super(context);
        this.random = new Random();
        this.queueManager = queueManagerActor;
        this.library = library;
        this.timers = timers;
        this.singerNumber = singerNumber;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ArtistsMessage.class, this::onArtistsMessage)
                .onMessage(SongsMessage.class, this::onSongsMessage)
                .onMessage(StartSingingMessage.class, this::onStartSingingMessage)
                .onMessage(sendListArtistsMessage.class, this::onSendListArtists)
                .build();
    }

    // sendet einen zufälligen Artist an die Library
    private Behavior<Message> onArtistsMessage(ArtistsMessage msg) {
        int randomArtistIndex = random.nextInt(((msg.artistsList.size())));
        library.tell(new LibraryActor.GetSongsMessage(this.getContext().getSelf(), msg.artistsList.get(randomArtistIndex), singerNumber));
        this.getContext().getLog().info(String.format("Singer %d chose %s", singerNumber, msg.artistsList.get(randomArtistIndex)));
        return this;
    }

    // sendet einen zufälligen Song an die Library
    private Behavior<Message> onSongsMessage(SongsMessage msg) {
        int randomSongIndex = random.nextInt(msg.songsList.size());
        queueManager.tell(new QueueManagerActor.AddMessage(this.getContext().getSelf(), msg.songsList.get(randomSongIndex), singerNumber));
        this.getContext().getLog().info(String.format("Singer %d chose '%s'", singerNumber, msg.songsList.get(randomSongIndex)));
        return this;
    }

    // setzt einen Timer und simuliert das Abspielen eines Songs
    private Behavior<Message> onStartSingingMessage(StartSingingMessage msg) {
        this.getContext().getLog().info(String.format("Singer %d starts singing: %s - '%s' for %d seconds", singerNumber, msg.artistName, msg.title, msg.duration));
        timers.startSingleTimer("Singer is singing...", sendListArtistsMessage.INSTANCE, Duration.ofSeconds(msg.duration));
        return this;
    }

    // sendet eine Anfrage auf die Liste von Artists an die Library
    private Behavior<Message> onSendListArtists(sendListArtistsMessage msg) {
        library.tell(new LibraryActor.ListArtistsMessage(this.getContext().getSelf(), singerNumber));
        return this;
    }
}