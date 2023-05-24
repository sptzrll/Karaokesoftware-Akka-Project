// Alla Spitzer 222114
// Olha Borysova 230606
// Anastasiia Kulyani 230612
// Dmytro Pahuba 230665

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
    private final Random random;
    private final long minTime;
    private final long maxTime;
    private int singerNumber;

    public interface Message {}

    public record CreateSingerMessage() implements Message {}

    public static Behavior<Message> create(ActorRef<QueueManagerActor.Message> queueManager, ActorRef<LibraryActor.Message> library) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers ->
                new SpawnerActor(context, timers, queueManager, library)));
    }

    private SpawnerActor(
            ActorContext<Message> context,
            TimerScheduler<SpawnerActor.Message> timers,
            ActorRef<QueueManagerActor.Message> queueManager,
            ActorRef<LibraryActor.Message> library
    ) {
        super(context);
        this.timers = timers;
        this.random = new Random();
        SpawnerActor.queueManager = queueManager;
        SpawnerActor.library = library;

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

    // erstellt einen neuen KaraokeSinger
    private Behavior<Message> onCreateSingerMessage(CreateSingerMessage msg) {
        ActorRef<KaraokeSingerActor.Message> singer = this.getContext().spawn(KaraokeSingerActor.create(queueManager, library, singerNumber), String.format("singer%d", singerNumber));
        this.getContext().getLog().info(String.format("Singer %d was created.", singerNumber));
        library.tell(new LibraryActor.ListArtistsMessage(singer, singerNumber));
        singerNumber++;
        createRandomDuration(this.minTime, this.maxTime);
        return this;
    }

    // setzt einen Timer für das Erstellen des neuen KaraokeSinger
    private void createRandomDuration(long minimum, long maximum){
        long interval = random.nextInt((int) ((maximum - minimum) + 1)) + minimum;
        Message tempMessage = new CreateSingerMessage();
        Duration durationTime = Duration.ofSeconds(interval);
        this.timers.startSingleTimer(tempMessage, durationTime);
    }
}