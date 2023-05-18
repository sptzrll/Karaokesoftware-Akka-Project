package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    public static class Create {
    }

    public static Behavior<Create> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Create> context) {
        super(context);
    }

    @Override
    public Receive<Create> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).build();
    }

    private Behavior<Create> onCreate(Create command) {
        //#create-actors
        ActorRef<LibraryActor.Message> library = this.getContext().spawn(LibraryActor.create(), "library");
        ActorRef<SpawnerActor.Message> spawner = this.getContext().spawn(SpawnerActor.create(), "spawner");
        ActorRef<PlaybackClientActor.Message> playbackClient = this.getContext().spawn(PlaybackClientActor.create(), "playbackClient");
        ActorRef<QueueManagerActor.Message> queueManager = this.getContext().spawn(QueueManagerActor.create(), "queueManager");
        //#create-actors

        library.tell(new LibraryActor.CreateMessage(this.getContext().getSelf()));
//        a.tell(new ExampleActor.ExampleMessage(this.getContext().getSelf(),"Test123"));
//        b.tell(new ExampleTimerActor.ExampleMessage("TestB"));
        return this;
    }
}
