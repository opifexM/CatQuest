package com.javarush.maximov.servlet;

import com.javarush.maximov.App;
import com.javarush.maximov.constants.Attribute;
import com.javarush.maximov.constants.Location;
import com.javarush.maximov.constants.Message;
import com.javarush.maximov.settings.GameStatus;
import com.javarush.maximov.settings.LandType;
import com.javarush.maximov.util.UrlParser;
import com.javarush.maximov.world.Cell;
import com.javarush.maximov.world.Game;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class GameServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(GameServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletContext context = request.getServletContext();
        Connection connection = (Connection) context.getAttribute(Attribute.DB_CONNECTION);
        HttpSession session = request.getSession();

        if (connection == null || session == null) {
            log.error("Connection or session are null.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Game game = App.getGame(session.getId());
        if (game == null) {
            session.setAttribute(Attribute.FLASH, Message.NEED_NEW_GAME);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_DANGER);
            response.sendRedirect(Location.ROOT);
            return;
        }

        listMap(request, response, game, session);
    }

    private void listMap(HttpServletRequest request, HttpServletResponse response,
                         Game game, HttpSession session) throws ServletException, IOException {

        log.info("Show map.");
        Collection<Cell> values = game.getCellList().values();
        request.setAttribute(Attribute.MAP, values);
        request.setAttribute(Attribute.GAME, game);

        if (game.getStatus() == GameStatus.NOTHING) {
            session.setAttribute(Attribute.GAMEFLASH, Message.HELP_START_NEW_GAME);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_INFO);
            log.info("Can't show map. No active game.");

        } else if (game.getStatus() == GameStatus.VICTORY) {
            session.setAttribute(Attribute.GAMEFLASH, Message.WIN_GAME);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_SUCCESS);
            log.info("Can't show map. Game is over victory.");

        } else if (game.getStatus() == GameStatus.DEFEAT) {
            session.setAttribute(Attribute.GAMEFLASH, Message.LOSE_GAME);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_DANGER);
            log.info("Can't show map. game is over defeat.");
        }

        RequestDispatcher requestDispatcher = request.getRequestDispatcher(Location.GAME_JSP);
        requestDispatcher.forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletContext context = request.getServletContext();
        Connection connection = (Connection) context.getAttribute(Attribute.DB_CONNECTION);
        HttpSession session = request.getSession();

        if (connection == null || session == null) {
            log.error("Connection or session are null.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Game game = App.getGame(session.getId());
        if (game == null) {
            log.info("Can't do Game POST. No active game.");
            session.setAttribute(Attribute.FLASH, Message.NEED_NEW_GAME);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_DANGER);
            response.sendRedirect(Location.ROOT);
            return;
        }

        switch (UrlParser.getAction(request)) {
            case "move" -> moveToCell(request, response, connection, game, session);
            case "action" -> actonCell(request, response, game, session);
            case "retreat" -> retreatCell(response, session);
            default -> listMap(request, response, game, session);
        }
    }

    private void retreatCell(HttpServletResponse response, HttpSession session) throws IOException {
        log.info("Set Retreat status.");
        session.setAttribute(Attribute.RETREAT, true);
        response.sendRedirect(Location.GAME);
    }

    private void moveToCell(HttpServletRequest request, HttpServletResponse response,
                            Connection connection, Game game, HttpSession session) throws IOException {

        String id = request.getParameter(Attribute.ID);
        if (id.isEmpty()) {
            log.info("Cell is not selected. No ID cell.");
            response.sendRedirect(Location.GAME);
            return;
        }

        int cellId = Integer.parseInt(id);
        Map<Integer, Cell> cellList = game.getCellList();
        Cell currentCell = cellList.get(cellId);

        if (!currentCell.isAvailable() && !game.isAdminAccess()) {
            session.setAttribute(Attribute.GAMEFLASH, Message.CANT_MOVE);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_DANGER);
            log.info("Can't move to cell {}. Cell is not available.", cellId);

        } else if (currentCell.getLandType() == LandType.WALL) {
            session.setAttribute(Attribute.GAMEFLASH, Message.CANT_MOVE_WALL);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_DANGER);
            log.info("Can't move to cell {}. Cell is wall.", cellId);

        } else if (currentCell.getLandType() == LandType.ENTRANCE && game.getStatus() == GameStatus.NOTHING) {
            game.setStatus(GameStatus.PLAY);
            log.info("Game status change to PLAY.");

        } else if (currentCell.checkEnemyInCell()
                && enemyCellSetAttribute(response, connection, session, cellId, currentCell)) {
            return;

        } else if (currentCell.checkRewardInCell()
                && rewardCellSetAttribute(response, connection, session, cellId, currentCell))
            return;

        session.setAttribute(Attribute.CURRENTCELL, currentCell);
        response.sendRedirect(Location.GAME);
    }

    private static boolean rewardCellSetAttribute(HttpServletResponse response, Connection connection,
                                                  HttpSession session, int cellId, Cell currentCell)
            throws IOException {
        int rewardId = currentCell.getRewardList().get(0).getId();
        int rewardCount = currentCell.getRewardList().size();

        String query = "SELECT name, comment FROM reward WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, rewardId);
            ResultSet rs = statement.executeQuery();
            if (!rs.first()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return true;
            }
            String rewardName = rs.getString("name");
            String rewardComment = rs.getString("comment");
            session.setAttribute("rewardname", rewardName);
            session.setAttribute("rewardcomment", rewardComment);
            session.setAttribute("rewardcount", rewardCount);
            session.setAttribute("rewardid", rewardId);
            log.info("Get reward id {} for cell {}", rewardId, cellId);

        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return true;
        }
        return false;
    }

    private static boolean enemyCellSetAttribute(HttpServletResponse response, Connection connection,
                                                 HttpSession session, int cellId, Cell currentCell)
            throws IOException {
        int enemyId = currentCell.getEnemyList().get(0).getId();
        int enemyCount = currentCell.getEnemyList().size();

        String query = "SELECT name, comment FROM enemy WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, enemyId);
            ResultSet rs = statement.executeQuery();
            if (!rs.first()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return true;
            }
            String enemyName = rs.getString("name");
            String enemyComment = rs.getString("comment");
            session.setAttribute("enemyname", enemyName);
            session.setAttribute("enemycomment", enemyComment);
            session.setAttribute("enemycount", enemyCount);
            session.setAttribute("enemyid", enemyId);
            log.info("Get enemy id {} for cell {}", enemyId, cellId);

        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return true;
        }
        return false;
    }

    private void actonCell(HttpServletRequest request, HttpServletResponse response,
                           Game game, HttpSession session) throws IOException {
        String id = request.getParameter(Attribute.ID);
        if (id.isEmpty() && game.getTurn() == 0) {
            Optional<Cell> entranceCell = game.getCellList().values().stream()
                    .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
            if (entranceCell.isPresent()) {
                id = String.valueOf(entranceCell.get().getId());
            }
        }

        if (id.isEmpty()) {
            log.info("Cell is not selected. No ID cell.");
            response.sendRedirect(Location.GAME);
            return;
        }

        int cellId = Integer.parseInt(id);
        Map<Integer, Cell> cellList = game.getCellList();
        Cell currentCell = cellList.get(cellId);

        if (currentCell.getLandType() == LandType.ENTRANCE) {
            game.setStatus(GameStatus.PLAY);
            session.setAttribute(Attribute.GAMEFLASH, Message.HELP_NEXT_TURN);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_INFO);
        } else if (currentCell.getLandType() == LandType.EXIT) {
            log.info("Game status set to VICTORY.");
            game.setStatus(GameStatus.VICTORY);
            response.sendRedirect(Location.ROOT);
            return;
        }

        setAvailableAroundCells(currentCell.getX(), currentCell.getY(), game);
        currentCell.setExplore(true);
        game.nextTurn();
        log.info("Cell action for id: {}", cellId);
        response.sendRedirect(Location.GAME);
    }


    public void setAvailableAroundCells(int x, int y, Game game) {
        setCellAvailability(x, y, game);
        setCellAvailability(x, y + 1, game);
        setCellAvailability(x, y - 1, game);
        setCellAvailability(x + 1, y, game);
        setCellAvailability(x - 1, y, game);
    }

    private void setCellAvailability(int x, int y, Game game) {
        if (x > 0 && y > 0
                && x <= game.getMapGenerator().getMapSize()
                && y <= game.getMapGenerator().getMapSize())
        {
            int id = Integer.parseInt(x + "" + y);
            if (game.getCellList().containsKey(id)) {
                log.info("Set available for cell id: {}", id);
                game.getCellList().get(id).setAvailable(true);
            }
        }
    }
}
