package com.javarush.maximov.world;

import com.javarush.maximov.settings.LandType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Getter
@Setter
public class Cell {
    private int id;
    private int x;
    private int y;
    private int generationWallLevel;
    private LandType landType;
    private boolean endLine;
    private boolean test;
    private boolean startLine;
    private boolean explore = false;
    private boolean available = false;
    private List<Enemy> enemyList = new ArrayList<>();
    private List<Reward> rewardList = new ArrayList<>();


    public Cell() {
        // needs for JSON parser
    }

    public Cell(int id, int x, int y, int generationWallLevel, LandType landType, boolean endLine, boolean startLine) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.generationWallLevel = generationWallLevel;
        this.landType = landType;
        this.endLine = endLine;
        this.startLine = startLine;
    }

    public void addEnemyToList(Enemy enemy) {
        enemyList.add(enemy);
    }

    public void addRewardToList(Reward reward) {
        rewardList.add(reward);
    }

    public void clearEnemyList() {
        enemyList.clear();
    }

    public void clearRewardList() {
        rewardList.clear();
    }

    public boolean checkRewardInCell() {
        return !rewardList.isEmpty();
    }

    public boolean checkEnemyInCell() {
        return !enemyList.isEmpty();
    }

    }
