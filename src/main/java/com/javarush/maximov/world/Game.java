package com.javarush.maximov.world;

import com.javarush.maximov.engine.MapGenerator;
import com.javarush.maximov.settings.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public class Game {
    private final LocalDateTime start = LocalDateTime.now();
    private LocalDateTime lastPlay;
    private int turn;
    private GameStatus status = GameStatus.NOTHING;
    private boolean adminAccess = false;
    private final Map<Integer, Cell> cellList = new TreeMap<>();
    private MapGenerator mapGenerator;

    public Game() {
        // needs for JSON parser
    }

   public void nextTurn() {
        turn++;
    }

}
