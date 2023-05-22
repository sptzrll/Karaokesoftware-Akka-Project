package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.LinkedList;
import java.util.Queue;


public class QueueManagerActor extends AbstractBehavior<QueueManagerActor.Message> {

    private Queue<String> playlist;
    private Queue<ActorRef<KaraokeSingerActor.Message>> singers;
    private ActorRef<PlaybackClientActor.Message> playback;


    public interface Message { }

    public record StartMessage(ActorRef<PlaybackClientActor.Message> playback) implements Message {}
    public record ReadyMessage(String string) implements Message { }
    public record AddMessage(ActorRef<KaraokeSingerActor.Message> singer, String songName, int singerNumber) implements Message { }

    public static Behavior<Message> create() {
        return Behaviors.setup(QueueManagerActor::new);
    }

    private QueueManagerActor(ActorContext<Message> context) {
        super(context);
        this.playlist = new LinkedList<>();
        this.singers = new LinkedList<>();
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartMessage.class, this::onStartMessage)
                .onMessage(ReadyMessage.class, this::onReadyMessage)
                .onMessage(AddMessage.class, this::onAddMessage)
                .build();
    }

    private Behavior<Message> onStartMessage(StartMessage msg) {
        this.playback = msg.playback;
        return this;
    }

    private Behavior<Message> onReadyMessage(ReadyMessage msg) {
        if (!this.playlist.isEmpty()) {
            String firstSong = playlist.poll();
            ActorRef<KaraokeSingerActor.Message> singer = singers.poll();
            playback.tell(new PlaybackClientActor.PlayMessage(firstSong, singer));
        }
        return this;
    }

    private Behavior<Message> onAddMessage(AddMessage msg) {
        if (!this.playlist.isEmpty()) {
            playlist.add(msg.songName);
            singers.add(msg.singer);
            this.getContext().getLog().info("QueueManager added '" + msg.songName + "' to Queue.");
        } else {
            playback.tell(new PlaybackClientActor.PlayMessage(msg.songName, msg.singer));
            this.getContext().getLog().info("QueueManger sent '"+ msg.songName + "' to PlaybackClient.");
        }
        return this;
    }
}