// Alla Spitzer 222114
// Olha Borysova 230606
// Anastasiia Kulyani 230612
// Dmytro Pahuba 230665

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
        ActorRef<QueueManagerActor.Message> queueManager = this.getContext().spawn(QueueManagerActor.create(), "queueManager");
        ActorRef<PlaybackClientActor.Message> playbackClient = this.getContext().spawn(PlaybackClientActor.create(queueManager), "playbackClient");
        ActorRef<SpawnerActor.Message> spawner = this.getContext().spawn(SpawnerActor.create(queueManager, library), "spawner");
        //#create-actors

        library.tell(new LibraryActor.Start(this.getContext().getSelf(), playbackClient));
        queueManager.tell(new QueueManagerActor.StartMessage(playbackClient));

        return this;
    }
}