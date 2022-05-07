# Summary

Scalable Reliable Multicast (SRM) is a negative acknowledgement (NACK) -oriented reliable multicast framework ([Floyd et al, 1997](https://doi.org/10.1109/90.650139)). Algorithm ensures receiver-based model of reliability for asynchronous systems on the application level. By referring to term “reliable”, we guarantee properties of integrity, validity and agreement, i.e., if one non-faulty process delivers a message `m`, then `m` will be eventually delivered (liveness) to all group members with no duplication, unless all nodes holding `m` have already crashed. However, ordering isn’t fulfilled. We investigate related research on this topic and compare SRM to other approaches. The loss recovery method is adaptive to the length of repair delay and number of duplicates, making SRM efficient across a broad variety of underlying network typologies.

<p align="center">
  <img src="/docs/srm_paper.png" width="600">
</p>

Furthermore, we develop a distributed game application that runs on top of the framework, similar to that [Draw & Guess on Steam](https://store.steampowered.com/app/1483870/Draw__Guess/). A player draws a word, the next player guesses the word that the drawing wants to represent, then another person draws the guess again. Continue the loop, let’s see if the last player guessed the first word correctly.

<p align="center">
  <img src="/docs/dag_steam.png" width="600">
</p>

# Repo Structure

```
├── docs/
|   └──── ..
├── libs/
|   └──── gson-2.9.0.jar
├── src/
|   ├──── app/                                 # application
|   |     ├──── UI_util/
|   |     |     ├──── ColorLine.java
|   |     |     ├──── ColorPoint.java
|   |     |     ├──── MyMouseAdapter.java
|   |     |     ├──── PlayerRenderer.java
|   |     |     ├──── RoomRenderer.java
|   |     |     └──── VocabRenderer.java
|   |     ├──── socket_threads/
|   |     |     ├──── lobby_group/
|   |     |     |     ├──── InLobbyAdvertiseThread.java
|   |     |     |     └──── InLobbyReceiveThread.java
|   |     |     └──── room_group/
|   |     |           ├──── InRoomAdvertiseThread.java
|   |     |           └──── InRoomReceiveThread.java
|   |     ├──── DataCache.java
|   |     ├──── DrawPane.java
|   |     ├──── DrawandGuess.java              # main
|   |     ├──── EndGamePane.java
|   |     ├──── GuessPane.java
|   |     ├──── LobbyPane.java
|   |     ├──── MySocketFactory.java
|   |     ├──── Player.java
|   |     ├──── Room.java
|   |     ├──── ShowPane.java
|   |     ├──── WaitingPane.java
|   |     ├──── WaitingRoomPane.java
|   |     ├──── WelcomePane.java
|   |     └──── WhiteBoardGUI.java
|   └──── srm/                                 # framework
|         ├──── DataCache.java
|         ├──── Message.java
|         ├──── ReceiverDispatcher.java
|         ├──── ReliableMulticastSocket.java   # facade
|         ├──── RequestRepairPool.java
|         ├──── StateTable.java
|         └──── Type.java
├── .gitattributes
├── .gitignore
├── LICENSE
├── README
├── vocab1.txt                                  # example vocabulary file
└── vocab2.txt
```

# Library

https://search.maven.org/artifact/com.google.code.gson/gson/2.9.0/jar
