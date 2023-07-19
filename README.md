# Karaokesoftware - README
This small project is a karaoke software developed using the Akka framework, with a focus on modeling concurrent processes. The system consists of different actors that work together to manage the karaoke playlist and allow karaoke singers to perform.

## Actors in the System
1. **LibraryActor:** This actor manages the available songs. Each song has an artist, a title, and a duration in seconds. For this prototype, song durations are rounded by dividing them by 30. The LibraryActor can receive two types of messages:
    **ListArtists:** Upon receiving this message, the LibraryActor will send a list of all stored artists back to the sender.
    **GetSongs(artistName):** When the LibraryActor receives this message, it will send all songs by the specified artist back to the sender.

2. **QueueManagerActor:** The QueueManagerActor handles the karaoke playlist. It can receive two types of messages:
    **Ready:** Sent by the PlaybackClientActor to indicate that it is ready to play a song. If the playlist contains at least one song, the QueueManagerActor will remove the first song from the playlist and send it to the PlaybackClientActor for playback.
    **Add(song):** Contains a song that needs to be added to the playlist. If the playlist is empty and no song is currently playing, the song will be sent directly to the PlaybackClientActor. A log entry will be made to record the song's title and whether it will be played immediately or added to the queue.

3. **KaraokeSingerActor:** Multiple KaraokeSingerActors represent the karaoke singers. They can receive three types of messages:
    **Artists:** Sent upon creation to request a list of all artists from the LibraryActor. The KaraokeSingerActor will randomly select an artist and request their songs from the LibraryActor. A random song will then be selected from this list and added to the queue using the QueueManagerActor's Add message.
    **Songs:** Upon receiving this message from the LibraryActor, the KaraokeSingerActor will have a list of songs by a specific artist.
    **StartSinging:** Sent by the SpawnerActor to initiate the singing process. Upon receiving this message, the KaraokeSingerActor will start singing the selected song and create a log entry with the artist, title, and duration of the song.

4. **SpawnerActor:** The SpawnerActor is responsible for creating new KaraokeSingerActors at random intervals of 2-12 seconds.

5. **PlaybackClientActor:** The PlaybackClientActor simulates the playback of a song. It can receive a **Play(song)** message containing a song to be played. Upon receiving this message, the PlaybackClientActor will send a **StartSinging(artist, title, duration)** message to the KaraokeSingerActor who added the song to the playlist. After waiting for the duration of the song, a log entry will be made with "Done," and a Ready message will be sent to the QueueManagerActor.

## How to rum
1. Clone the project.
2. Ensure that you have Akka and any necessary dependencies installed.
3. Build the project with gradle.
4. Start the karaoke software by running the **AkkaStart** application.
