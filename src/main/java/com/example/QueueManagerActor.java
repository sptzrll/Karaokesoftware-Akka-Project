package com.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class QueueManagerActor extends AbstractBehavior<QueueManagerActor.Message> {

    private List<Song> wiedergabe;
    public interface Message {};

    public static class ReadyMessage implements QueueManagerActor.Message {
    }
    public static class AddMessage implements QueueManagerActor.Message {
    }


    public record ExampleMessage(String someString) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new QueueManagerActor(context, timers)));
    }


    private QueueManagerActor(ActorContext<Message> context, TimerScheduler<QueueManagerActor.Message> timers) {
        super(context);
        wiedergabe = new ArrayList<>();

    }

    @Override
    public Receive<QueueManagerActor.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(QueueManagerActor.ReadyMessage.class, this::onReadyMessage)
                .onMessage(QueueManagerActor.AddMessage.class, this::onAddMessage)
                .build();
    }

    private Behavior<Message> onReadyMessage(ReadyMessage msg) {
        getContext().getLog().info("");
        return this;
    }

    private Behavior<Message> onAddMessage(AddMessage msg) {
        getContext().getLog().info("");
        return this;
    }
}
