package com.javarush.maximov.world;

import lombok.Getter;

@SuppressWarnings("unused")
@Getter
public class Reward {
    private int id;

    public Reward() {
        // needs for JSON parser
    }

    public Reward(int id) {
        this.id = id;
    }
}
