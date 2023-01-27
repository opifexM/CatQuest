package com.javarush.maximov.world;

import lombok.*;

@SuppressWarnings("unused")
@Setter
@Getter
public class Enemy {
    private int id;
    private int number;
    private int currentHealth;
    private int currentAttack;
    private int currentDefense;

    public Enemy() {
        // needs for JSON parser
    }

    public Enemy(int id, int number) {
        this.id = id;
        this.number = number;
    }
}
