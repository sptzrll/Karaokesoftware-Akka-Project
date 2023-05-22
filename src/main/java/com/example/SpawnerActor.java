package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Random;


public class SpawnerActor extends AbstractBehavior<SpawnerActor.Message> {

    private final TimerScheduler<SpawnerActor.Message> timers;
    private static ActorRef<QueueManagerActor.Message> queueManager;
    private static ActorRef<LibraryActor.Message> library;
    private ActorRef<PlaybackClientActor.Message> playback;
    private final Random random;
    private final long minTime;
    private final long maxTime;
    private int singerNumber;

    public interface Message {}

    public record CreateSingerMessage() implements Message {}

    public static Behavior<Message> create(ActorRef<QueueManagerActor.Message> queueManager, ActorRef<LibraryActor.Message> library, ActorRef<PlaybackClientActor.Message> playback) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers ->
                new SpawnerActor(context, timers, queueManager, library, playback)));
    }

    private SpawnerActor(
            ActorContext<Message> context,
            TimerScheduler<SpawnerActor.Message> timers,
            ActorRef<QueueManagerActor.Message> queueManager,
            ActorRef<LibraryActor.Message> library,
            ActorRef<PlaybackClientActor.Message> playback
    ) {
        super(context);
        this.timers = timers;
        this.random = new Random();
        this.queueManager = queueManager;
        this.library = library;
        this.playback = playback;

        this.minTime = 2;
        this.maxTime = 12;
        this.singerNumber = 1;

        createRandomDuration(this.minTime, this.maxTime);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(CreateSingerMessage.class, this::onCreateSingerMessage)
                .build();
    }

    private Behavior<Message> onCreateSingerMessage(CreateSingerMessage msg) {
        ActorRef<KaraokeSingerActor.Message> singer = this.getContext().spawn(KaraokeSingerActor.create(queueManager, library, playback, singerNumber), String.format("singer%d", singerNumber));
        this.getContext().getLog().info(String.format("Singer %d was created.", singerNumber));
        library.tell(new LibraryActor.ListArtistsMessage(singer, singerNumber));
        singerNumber++;
        createRandomDuration(this.minTime, this.maxTime);
        return this;
    }

    private void createRandomDuration(long minimum, long maximum){
        long interval = random.nextInt((int) ((maximum - minimum) + 1)) + minimum;
        Message tempMessage = new CreateSingerMessage();
        Duration durationTime = Duration.ofSeconds(interval);
        this.timers.startSingleTimer(tempMessage, durationTime);
    }
}