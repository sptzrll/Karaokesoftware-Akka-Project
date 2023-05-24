// Alla Spitzer 222114
// Olha Borysova 230606
// Anastasiia Kulyani 230612
// Dmytro Pahuba 230665

package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.List;


public class PlaybackClientActor extends AbstractBehavior<PlaybackClientActor.Message> {

    public interface Message {}

    public record GetList(List<Song> songList) implements Message{}

    public record PlayMessage(String songName, ActorRef<KaraokeSingerActor.Message> singer) implements Message { }
    public enum sendReadyMessage implements Message { INSTANCE }

    public static Behavior<Message> create(ActorRef<QueueManagerActor.Message> queueManager) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new PlaybackClientActor(context, timers, queueManager)));
    }

    private final TimerScheduler<PlaybackClientActor.Message> timers;
    private final ActorRef<QueueManagerActor.Message> queueManager;
    private List<Song> songList;

    private PlaybackClientActor(ActorContext<Message> context, TimerScheduler<PlaybackClientActor.Message> timers, ActorRef<QueueManagerActor.Message> queueManager) {
        super(context);
        this.timers = timers;
        this.queueManager = queueManager;
        queueManager.tell(new QueueManagerActor.ReadyMessage("Ready"));
        this.getContext().getLog().info("Ready");
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetList.class, this::onGetList)
                .onMessage(PlayMessage.class, this::onPlayMessage)
                .onMessage(sendReadyMessage.class, this::onSendReadyMessage)
                .build();
    }

    /*
     *  liefert eine Liste mit allen Songs
     */
    private Behavior<Message> onGetList(GetList msg) {
        this.songList = msg.songList;
        return this;
    }

    /*
     *   sendet eine StartSinging-Nachricht an den KaraokeSinger, setzt einen Timer und simuliert das Abspielen eines Songs
     */
    private Behavior<Message> onPlayMessage(PlayMessage msg) {

        int duration = 0;
        for (Song song: songList) {
            if(song.getTitle().equals(msg.songName)){
                duration = song.getDuration();
                msg.singer.tell(new KaraokeSingerActor.StartSingingMessage(song.getArtistName(), song.getTitle(), song.getDuration()));
                break;
            }
        }
        //Nach dem Ablauf des Timers sendet der Timer eine onSendReadyMessage an den PlaybackClient
        timers.startSingleTimer("Song is playing...", sendReadyMessage.INSTANCE, Duration.ofSeconds(duration));
        this.getContext().getLog().info("Done");
        return this;
    }
    /*
     *   sendet eine Ready-Nachricht an den QueueClient
     */
    private Behavior<Message> onSendReadyMessage(sendReadyMessage msg) {
        queueManager.tell(new QueueManagerActor.ReadyMessage("Ready"));
        return this;
    }
}