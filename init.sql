DROP TABLE IF EXISTS reward;
DROP TABLE IF EXISTS enemy;
DROP TABLE IF EXISTS spell;
DROP TABLE IF EXISTS save;

create table reward
(
    id      INTEGER,
    name    TINYTEXT,
    comment TEXT,
    health  INTEGER,
    attack  INTEGER,
    defense INTEGER,
    mana    INTEGER,
    minor   BOOLEAN
);

INSERT INTO reward
    (id, name, comment, health, attack, defense, mana, minor)
VALUES (1, 'Small Health Bottle', 'Allows you to instantly restore a little of health', 15, 0, 0, 0, true),
       (2, 'Health Bottle', 'Allows you to instantly restore a little of health', 30, 0, 0, 0, false),
       (3, 'Big Health Bottle', 'Allows you to instantly restore a lot of health', 50, 0, 0, 0, false),
       (4, 'Small Mana Bottle', 'Allows you to instantly restore a little of mana', 0, 0, 0, 30, true),
       (5, 'Mana Bottle', 'Allows you to instantly restore a little of mana', 0, 0, 0, 45, false),
       (6, 'Big Mana Bottle', 'Allows you to instantly restore a lot of mana', 50, 0, 0, 70, false),
       (7, 'Small Attack Bottle', 'Allows you to increase the attack power slightly.', 0, 10, 0, 0, true),
       (8, 'Big Attack Bottle', 'Allows you to increase the attack power significantly.', 0, 40, 0, 0, false),
       (9, 'Small Defense Bottle', 'Allows you to increase the defense power slightly.', 0, 0, 20, 0, true),
       (10, 'Big Defense Bottle', 'Allows you to increase the defense power significantly.', 0, 0, 70, 0, false),
       (11, 'Power Bottle', 'Increases the parameters of both defense and attack.', 0, 25, 40, 0, false),
       (12, 'Live Bottle', 'Increases the parameters of both health and mana.', 40, 0, 0, 60, false);

create table enemy
(
    id            INTEGER,
    name          TINYTEXT,
    comment       TEXT,
    pack          INTEGER,
    health        INTEGER,
    attack        INTEGER,
    defense       INTEGER,
    magic_health  BOOLEAN,
    magic_attack  BOOLEAN,
    magic_defense BOOLEAN
);

INSERT INTO enemy
(id, name, comment, pack, health, attack, defense, magic_health, magic_attack, magic_defense)
VALUES (1, 'Bunny', 'A weak opponent does not pose a threat.', 1, 25, 5, 5, false, false, false),
       (2, 'Bat', 'The enemy is weak but always attacks in a pack.', 5, 15, 10, 20, false, false, false),
       (3, 'Rat', 'A dangerous opponent always attacks in large numbers.', 10, 5, 5, 5, false, false, false),
       (4, 'Wolf', 'A high level of risk always attacks as part of a pack.', 3, 45, 35, 20, false, false, false),
       (5, 'Bear', 'A strong opponent with a high level of attack and defense.', 1, 100, 45, 60, false, false, false),
       (6, 'Basilisk', 'Very high level of protection but weak attack parameters.', 1, 200, 50, 65, false, false,
        false),
       (7, 'Magic Basilisk', 'Extremely high level of protection and health and the possibility of using magic.', 1,
        200, 50, 65, true, false, true),
       (8, 'Magic Fairy', 'Possesses magic but has weak defense.', 5, 20, 10, 30, true, true, false),
       (9, 'Magic Owl', 'Possesses the magic of defense and attack.', 2, 40, 30, 60, false, true, true),
       (10, 'Magic Dragon', 'A strong opponent with a high level of armor and attacks and also has magic.', 1, 200, 45,
        60, true, true, true);

create table spell
(
    id      INTEGER,
    name    TINYTEXT,
    comment TEXT,
    mana    INTEGER,
    health  INTEGER,
    attack  INTEGER,
    defense INTEGER,
    freezer INTEGER
);

INSERT INTO spell
    (id, name, comment, mana, health, attack, defense, freezer)
VALUES (1, 'Healing', 'Restores health.', 10, 20, 0, 0, 0),
       (2, 'Freezing', 'Immobilizes opponents on the next move.', 30, 0, 0, 0, 70),
       (3, 'Fireball', 'Carries a significant blow with the fiery element.', 20, 0, 25, 0, 0),
       (4, 'Stone Armor', 'Increases the protection parameters for the duration of the entire battle.', 15, 0, 0, 40,
        0),
       (5, 'Shining Blade', 'Increases the attacking parameters for the duration of the entire battle.', 15, 0, 30, 0,
        0);

create table save
(
    number      INTEGER,
    session TINYTEXT,
    turn    INTEGER,
    date    TINYTEXT,
    json    TEXT
);