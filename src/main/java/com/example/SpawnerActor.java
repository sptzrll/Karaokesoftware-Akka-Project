package com.example;

import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;


public class SpawnerActor extends AbstractBehavior<SpawnerActor.Message> {

    public interface Message {};


    public record ExampleMessage(String someString) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new SpawnerActor(context, timers)));
    }

    private final TimerScheduler<SpawnerActor.Message> timers;

    private SpawnerActor(ActorContext<Message> context, TimerScheduler<SpawnerActor.Message> timers) {
        super(context);
        this.timers = timers;

        Message msg = new ExampleMessage("test123");
        this.timers.startSingleTimer(msg, msg, Duration.ofSeconds(10));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ExampleMessage.class, this::onExampleMessage)
                .build();
    }

    private Behavior<Message> onExampleMessage(ExampleMessage msg) {
        getContext().getLog().info("I have send myself this message after 10 Seconds: {}", msg.someString);
        return this;
    }
}
