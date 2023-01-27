package com.javarush.maximov.engine;

import com.javarush.maximov.settings.LandType;
import com.javarush.maximov.world.Cell;
import com.javarush.maximov.world.Enemy;
import com.javarush.maximov.world.Reward;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.javarush.maximov.util.Randomizer.randomIntFromToNotInclude;


@Getter
public class MapGenerator {
    private static final Logger log = LoggerFactory.getLogger(MapGenerator.class);

    private int minLandEmpty;
    private int chanceReward;
    private int chanceEnemy;
    private int mapSize;

    public MapGenerator() {
    }

    public MapGenerator(int minLandEmpty, int chanceReward, int chanceEnemy, int mapSize) {
        this.minLandEmpty = minLandEmpty;
        this.chanceReward = chanceReward;
        this.chanceEnemy = chanceEnemy;
        this.mapSize = mapSize;
    }

    public void generate(Map<Integer, Cell> cellMap, Connection sqlConnection) {
        log.info("Start generate new Map. minLandEmpty {}, chanceReward {}, chanceEnemy {}, mapSize {}",
                minLandEmpty, chanceReward, chanceEnemy, mapSize);
        generateMap(cellMap);
        long pathWayCount = cellMap.values().stream().filter(cell -> cell.getLandType() == LandType.EMPTY).count();
        log.info("Pathway for map {}", pathWayCount);
        List<Integer> enemyIdList = loadEnemyId(sqlConnection);
        List<Integer> rewardIdList = loadRewardId(sqlConnection);
        setupMap(cellMap, enemyIdList, rewardIdList, sqlConnection);
        log.info("Finish generate Map");
    }



    private List<Integer> loadEnemyId(Connection sqlConnection)  {
        log.info("Load Enemy from SQL");
        String query = "SELECT id FROM enemy ORDER BY id";
        try (PreparedStatement statement = sqlConnection.prepareStatement(query)) {
            ResultSet rs = statement.executeQuery();
            List<Integer> enemyIdList = new ArrayList<>();
            while (rs.next()) {
                enemyIdList.add(rs.getInt("id"));
            }
            return enemyIdList;
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            throw new RuntimeException(e);
        }
    }

    private List<Integer> loadRewardId(Connection sqlConnection) {
        log.info("Load Reward from SQL");
        String query = "SELECT id FROM reward WHERE minor = false ORDER BY id";
        try (PreparedStatement statement = sqlConnection.prepareStatement(query)) {
            ResultSet rs = statement.executeQuery();
            List<Integer> rewardIdList = new ArrayList<>();
            while (rs.next()) {
                rewardIdList.add(rs.getInt("id"));
            }
            return rewardIdList;
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            throw new RuntimeException(e);
        }
    }

    private void setupMap(Map<Integer, Cell> cellMap, List<Integer> enemyIdList, List<Integer> rewardIdList,
                          Connection sqlConnection) {
        log.info("Setup map for Reward and Enemy");
        List<Cell> emptyCellList = cellMap.values().stream()
                .filter(cell -> cell.getLandType() == LandType.EMPTY).toList();
        for (Cell cell : emptyCellList) {
            cell.clearEnemyList();
            cell.clearRewardList();
            int randomChanceReward = randomIntFromToNotInclude(1, 101);
            int randomChanceEnemy = randomIntFromToNotInclude(1, 101);

            if (randomChanceReward <= chanceReward && randomChanceEnemy <= chanceEnemy) {
                createAddRewardToCell(rewardIdList, cell);
                createAddEnemyToCell(enemyIdList, sqlConnection, cell);
            } else if (randomChanceReward <= chanceReward) {
                createAddRewardToCell(rewardIdList, cell);
            } else if (randomChanceEnemy <= chanceEnemy) {
                createAddEnemyToCell(enemyIdList, sqlConnection, cell);
            }
        }
    }

    private static void createAddRewardToCell(List<Integer> rewardIdList, Cell cell) {
        Integer rewardId = rewardIdList.get(randomIntFromToNotInclude(0, rewardIdList.size()));
        cell.addRewardToList(new Reward(rewardId));
    }

    private static void createAddEnemyToCell(List<Integer> enemyIdList, Connection sqlConnection, Cell cell) {
        Integer enemyId = enemyIdList.get(randomIntFromToNotInclude(0, enemyIdList.size()));
        String query = "SELECT pack FROM enemy WHERE id = ?";

        try (PreparedStatement statement = sqlConnection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(enemyId));
            ResultSet rs = statement.executeQuery();
            if (!rs.first()) {
                log.error("Error access to SQL for {}", query);
                throw new RuntimeException("mapSettling randomChanceEnemy");
            }
            int enemyPack = rs.getInt("pack");
            for (int i = 1; i <= enemyPack; i++) {
                Enemy enemy = new Enemy(enemyId, i);
                cell.addEnemyToList(enemy);
            }
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            throw new RuntimeException(e);
        }
    }

    private void generateMap(Map<Integer, Cell> cellMap) {
        log.info("Generate map land");

        cellMap.clear();
        int start = randomIntFromToNotInclude(2, mapSize);
        int end = randomIntFromToNotInclude(2, mapSize);

        for (int x = 1; x <= mapSize; x++) {
            for (int y = 1; y <= mapSize; y++) {
                boolean isStartLine = (y == 1);
                boolean isEndLine = (y == mapSize);
                int id = Integer.parseInt(x + "" + y);
                if (x == 1 && y == end) {
                    cellMap.put(id, new Cell(id, x, y, 0, LandType.EXIT, isEndLine, isStartLine));
                } else if (x == mapSize && y == start) {
                    Cell cell = new Cell(id, x, y, 0, LandType.ENTRANCE, isEndLine, isStartLine);
                    cell.setAvailable(true);
                    cellMap.put(id, cell);
                } else {
                    cellMap.put(id, new Cell(id, x, y, 1, LandType.WALL, isEndLine, isStartLine));
                }
            }
        }
        boolean isExitFound = searchPath(cellMap, mapSize, start, 1);
        if (!isExitFound) {
            log.info("Exit not found. Restart.");
            generateMap(cellMap);
        }
        long pathWayCount = cellMap.values().stream().filter(cell -> cell.getLandType() == LandType.EMPTY).count();
        if (pathWayCount < minLandEmpty) {
            log.info("Map pathway less {}. Restart.", minLandEmpty);
            generateMap(cellMap);
        }
        searchPath(cellMap, mapSize, start, 2);
    }


    public boolean searchPath(Map<Integer, Cell> cellMap, int x, int y, int currentWallLevel) {
        log.info("Searching for map pathway");

        int moveX;
        int moveY;

        List<Cell> cellAvailabilityList = new ArrayList<>();
        addToCellPathList(cellMap, x, y + 1, currentWallLevel, cellAvailabilityList);
        addToCellPathList(cellMap, x, y - 1, currentWallLevel, cellAvailabilityList);
        addToCellPathList(cellMap, x + 1, y, currentWallLevel, cellAvailabilityList);
        addToCellPathList(cellMap, x - 1, y, currentWallLevel, cellAvailabilityList);

        if (!cellAvailabilityList.isEmpty()) {
            for (Cell cell : cellAvailabilityList) {
                if (cell.getLandType() == LandType.EXIT) {
                    log.info("Exit cell is found!");
                    return true;
                }
            }

            int path = randomIntFromToNotInclude(0, cellAvailabilityList.size());
            Cell cellMove = cellAvailabilityList.get(path);
            cellAvailabilityList.remove(path);
            cellMove.setGenerationWallLevel(0);
            cellMove.setLandType(LandType.EMPTY);
            moveX = cellMove.getX();
            moveY = cellMove.getY();

            for (Cell cell : cellAvailabilityList) {
                cell.setGenerationWallLevel(currentWallLevel + 1);
            }
        } else {
            log.info("Exit cell is not found. No exit way for current map!");
            return false;
        }
        return searchPath(cellMap, moveX, moveY, currentWallLevel);
    }

    private void addToCellPathList(Map<Integer, Cell> cellMap, int x, int y, int currentWallLevel,
                                   List<Cell> availableMapList) {
        if (x > 0 && y > 0 && x <= mapSize && y <= mapSize) {
            int id = Integer.parseInt(x + "" + y);
            if (cellMap.containsKey(id)) {
                LandType landType = cellMap.get(id).getLandType();
                int wallLevel = cellMap.get(id).getGenerationWallLevel();
                if (landType != LandType.EMPTY && landType != LandType.ENTRANCE
                        && (wallLevel <= currentWallLevel || landType == LandType.EXIT)) {
                    availableMapList.add(cellMap.get(id));
                }
            }
        }
    }
}