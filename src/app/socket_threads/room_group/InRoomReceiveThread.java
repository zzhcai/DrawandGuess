package app.socket_threads.room_group;

import app.*;
import srm.ReliableMulticastSocket;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Instant;
import java.util.Collections;

/**
 * This thread receives all incoming messages within the room.
 * Either a player object or a room object would be received.
 * Updates the information accordingly.
 */
public class InRoomReceiveThread extends Thread {
    public volatile boolean interrupted = false;
    @Override
    public void run() {
        ReliableMulticastSocket socket = MySocketFactory.newInstance(DrawandGuess.currentRoom.IP, DrawandGuess.currentRoom.port);
        System.out.println("Room receive thread started");
        while (!interrupted) {
            DatagramPacket p = new DatagramPacket(new byte[65507], 65507);
            socket.receive(p);
            System.out.println("received at room: " + new String(p.getData()));

            // We determine the type by parsing into one type and checking if a must-have field is null.
            Player player = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Player.class);
            if (player.name == null) {
                Room room = DrawandGuess.gson.fromJson(new String(p.getData(), 0, p.getLength()), Room.class);
                synchronized (DrawandGuess.currentRoom) {
                    DrawandGuess.currentRoom.roomName = room.roomName;
                    DrawandGuess.currentRoom.dictionary = room.dictionary;
                    DrawandGuess.currentRoom.host = room.host;
                    DrawandGuess.currentRoom.numRounds = room.numRounds;
                    DrawandGuess.currentRoom.timeLimit = room.timeLimit;
                    DrawandGuess.currentRoom.inGame = room.inGame;
                    DrawandGuess.currentRoom.lastRound = room.lastRound;
                    DrawandGuess.currentRoom.initWords = room.initWords;
                    DrawandGuess.currentRoom.notifyAll();

                    synchronized (DrawandGuess.self) {
                        if (DrawandGuess.currentRoom.inGame && !DrawandGuess.self.inGame) {
                            DrawandGuess.self.inGame = true;
                            WhiteBoardGUI.redirectTo(WhiteBoardGUI.waitingRoom, WhiteBoardGUI.drawPane);
                            int index = DrawandGuess.currentRoom.playerList.indexOf(DrawandGuess.self);
                            String initWord = (String) JOptionPane.showInputDialog(null,
                                    "Select starting word",
                                    "Starting word",
                                    JOptionPane.QUESTION_MESSAGE, null,
                                    DrawandGuess.currentRoom.initWords.get(index).toArray(),
                                    DrawandGuess.currentRoom.initWords.get(index).get(0));
                            if (initWord==null) {
                                initWord = DrawandGuess.currentRoom.initWords.get(index).get(0);
                            }
                            DrawandGuess.self.guessedList.add(initWord);
                            WhiteBoardGUI.setPrevWord("Starting word: " + initWord);
                        }
                    }

                }

            } else {
                player.lastActive = Instant.now().toEpochMilli();
                synchronized (DrawandGuess.currentRoom) {
                    DrawandGuess.currentRoom.playerList.remove(player);
                    DrawandGuess.currentRoom.playerList.add(player);
                    Collections.sort(DrawandGuess.currentRoom.playerList);
                    DrawandGuess.currentRoom.notifyAll();
                }
            }
            synchronized (DrawandGuess.currentRoom) {
                if (DrawandGuess.currentRoom.allDone(DrawandGuess.turn)) {
                    // even turn guess
                    if (DrawandGuess.turn % 2 == 0) {
                        WhiteBoardGUI.drawPane = new DrawPane();
                        WhiteBoardGUI.redirectTo(WhiteBoardGUI.wait, WhiteBoardGUI.drawPane);
                    } else {
                        WhiteBoardGUI.guessPane = new GuessPane();
                        WhiteBoardGUI.redirectTo(WhiteBoardGUI.wait, WhiteBoardGUI.guessPane);
                    }
                    DrawandGuess.turn++;
                }
            }
        }
        System.out.println("Room receive thread closed");
        try {
            socket.leaveGroup(DrawandGuess.LOBBY_SOCKET_ADDRESS, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
