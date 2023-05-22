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
//        Message msg = new PlayMessage("", "song", 10);
//        this.timers.startSingleTimer(msg, msg, Duration.ofSeconds(10));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetList.class, this::onGetList)
                .onMessage(PlayMessage.class, this::onPlayMessage)
                .build();
    }

    private Behavior<Message> onGetList(GetList msg) {
        this.songList = msg.songList;
        return this;
    }

    private Behavior<Message> onPlayMessage(PlayMessage msg) {
        try {
            int duration = 0;
            for (Song song: songList) {
                if(song.getTitle().equals(msg.songName)){
                    duration = song.getDuration();
                    msg.singer.tell(new KaraokeSingerActor.StartSingingMessage(song.getArtistName(), song.getTitle(), song.getDuration()));
                    break;
                }
            }
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.getContext().getLog().info("Done");

        queueManager.tell(new QueueManagerActor.ReadyMessage("Ready"));
        return this;
    }
}