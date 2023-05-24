// Alla Spitzer 222114
// Olha Borysova 230606
// Anastasiia Kulyani 230612
// Dmytro Pahuba 230665

package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.LinkedList;
import java.util.Queue;


public class QueueManagerActor extends AbstractBehavior<QueueManagerActor.Message> {

    private final Queue<String> playlist;
    private final Queue<ActorRef<KaraokeSingerActor.Message>> singers;

    private ActorRef<PlaybackClientActor.Message> playback;
    private boolean playbackIsReady;


    public interface Message { }

    public record StartMessage(ActorRef<PlaybackClientActor.Message> playback) implements Message {}
    public record ReadyMessage(String string) implements Message {}
    public record AddMessage(ActorRef<KaraokeSingerActor.Message> singer, String songName, int singerNumber) implements Message { }

    public static Behavior<Message> create() {
        return Behaviors.setup(QueueManagerActor::new);
    }

    private QueueManagerActor(ActorContext<Message> context) {
        super(context);
        this.playlist = new LinkedList<>();
        this.singers = new LinkedList<>();
        this.playbackIsReady = false;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartMessage.class, this::onStartMessage)
                .onMessage(ReadyMessage.class, this::onReadyMessage)
                .onMessage(AddMessage.class, this::onAddMessage)
                .build();
    }

    // liefert die Referenz von dem PlaybackClient
    private Behavior<Message> onStartMessage(StartMessage msg) {
        this.playback = msg.playback;
        return this;
    }

    // sendet den nächsten Song aus der Warteschlange an den PlaybackClient
    private Behavior<Message> onReadyMessage(ReadyMessage msg) {
        playbackIsReady = true;
        if (!this.playlist.isEmpty()) {
            playbackIsReady = false;
            String firstSong = playlist.poll();
            ActorRef<KaraokeSingerActor.Message> singer = singers.poll();
            playback.tell(new PlaybackClientActor.PlayMessage(firstSong, singer));
        }
        return this;
    }

    // fügt den von KaraokeSinger ausgewählten Song in die Warteschlange ein oder sendet ihn an den PlaybackClient
    private Behavior<Message> onAddMessage(AddMessage msg) {

        if(!playbackIsReady){
            playlist.add(msg.songName);
            singers.add(msg.singer);
            //Logausgabe
            this.getContext().getLog().info("QueueManager added '" + msg.songName + "' to Queue.");
        } else{
            playbackIsReady = false;
            //Logausgabe
            this.getContext().getLog().info("QueueManger sent '"+ msg.songName + "' to PlaybackClient.");
            playback.tell(new PlaybackClientActor.PlayMessage(msg.songName, msg.singer));
        }
        return this;
    }
}