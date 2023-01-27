package com.javarush.maximov.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.javarush.maximov.App;
import com.javarush.maximov.constants.Attribute;
import com.javarush.maximov.constants.Generator;
import com.javarush.maximov.constants.Location;
import com.javarush.maximov.constants.Message;
import com.javarush.maximov.engine.MapGenerator;
import com.javarush.maximov.settings.GameStatus;
import com.javarush.maximov.util.UrlParser;
import com.javarush.maximov.world.Game;
import com.javarush.maximov.world.Save;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MenuServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(MenuServlet.class);

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

        if (UrlParser.getAction(request).equals("load")) {
            listSaveGame(request, response, connection, session);
        } else {
            showMenu(request, response, game, session);
        }
    }

    private void listSaveGame(HttpServletRequest request, HttpServletResponse response,
                              Connection connection, HttpSession session) throws IOException, ServletException {

        log.info("List of save games.");
        String sessionId = session.getId();
        Integer saveGameTotal = getSaveGameSessionCount(response, connection, sessionId);
        if (saveGameTotal == null || saveGameTotal < 1) {
            session.setAttribute(Attribute.FLASH, Message.NO_SAVE_GAMES);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_DANGER);
            response.sendRedirect(Location.ROOT);
            return;
        }

        List<Save> saveList = new ArrayList<>();
        String query = "SELECT number, turn, date FROM save WHERE SESSION = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, sessionId);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int number = rs.getInt("number");
                int turn = rs.getInt("turn");
                String date = rs.getString("date");
                saveList.add(new Save(number, sessionId, turn, date));
            }
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            return;
        }

        request.setAttribute(Attribute.SAVES, saveList);
        request.setAttribute(Attribute.SESSIONID, sessionId);
        request.setAttribute(Attribute.SAVETOTAL, saveGameTotal);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(Location.LOAD_JSP);
        requestDispatcher.forward(request, response);
    }

    private void showMenu(HttpServletRequest request, HttpServletResponse response, Game game, HttpSession session)
            throws ServletException, IOException {

        log.info("Show Game Menu.");
        String sessionId = session.getId();
        request.setAttribute(Attribute.GAME, game);
        request.setAttribute(Attribute.SESSIONID, sessionId);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(Location.WELCOME_JSP);
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

        switch (UrlParser.getAction(request)) {
            case "new" -> newGame(response, game, connection, session);
            case "end" -> endGame(response, game, session);
            case "save" -> saveGame(response, connection, game, session);
            case "load" -> loadGame(request, response, connection, session);
            case "delete" -> deleteGame(request, response, connection, session);
            case "admin" -> adminAccess(response, game, session);
            default -> showMenu(request, response, game, session);
        }
    }

    private void adminAccess(HttpServletResponse response, Game game, HttpSession session) throws IOException {
        if (game.isAdminAccess()) {
            log.info("Admin Access set to FALSE.");
            game.setAdminAccess(false);
            session.setAttribute(Attribute.FLASH, Message.ADMIN_OFF);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_INFO);
        } else {
            log.info("Admin Access set to TRUE.");
            game.setAdminAccess(true);
            session.setAttribute(Attribute.FLASH, Message.ADMIN_ON);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_INFO);
        }
        response.sendRedirect(Location.GAME);
    }


    private static boolean isGameStart(boolean game, HttpSession session, HttpServletResponse response)
            throws IOException {
        if (game) {
            session.setAttribute(Attribute.FLASH, Message.NEED_NEW_GAME);
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_DANGER);
            response.sendRedirect(Location.ROOT);
            return true;
        }
        return false;
    }

    private static Integer getSaveGameSessionCount(HttpServletResponse response,
                                                   Connection connection, String sessionId) throws IOException {
        log.info("Count saves sessions in SQL.");
        String query = "SELECT COUNT(*) FROM save WHERE session = ?";
        int sessionCount;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, sessionId);
            ResultSet rs = statement.executeQuery();
            if (!rs.first()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            sessionCount = rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
        return sessionCount;
    }

    private static String gameToJson(Game game) throws JsonProcessingException {
        log.info("Parser Game Object to JSON.");
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(game);
    }

    private static Game jsonToGame(String jsonGame) throws JsonProcessingException {
        log.info("Parser JSON to Game Object.");
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.readValue(jsonGame, Game.class);
    }

    @SuppressWarnings("DataFlowIssue")
    private void saveGame(HttpServletResponse response, Connection connection, Game game, HttpSession session)
            throws IOException {
        log.info("Save game start.");

        if (isGameStart(game == null || game.getCellList().size() == 0, session, response)) return;

        String gameJson = gameToJson(game);
        String sessionId = session.getId();

        Integer saveGameTotal = getSaveGameSessionCount(response, connection, sessionId);
        if (saveGameTotal == null) return;
        saveGameTotal++;

        if (isGameStart(saveGameTotal > 10, session, response)) return;

        String query = "INSERT INTO save (number, session, turn, date, json) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, saveGameTotal);
            statement.setString(2, sessionId);
            statement.setInt(3, game.getTurn());
            statement.setString(4, String.valueOf(LocalDateTime.now()));
            statement.setString(5, gameJson);
            statement.executeUpdate();

            session.setAttribute(Attribute.FLASH,
                    "The Game #" + saveGameTotal + " (Turn "+ game.getTurn() +") has been saved successfully.");
            session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_SUCCESS);
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        log.info("Save game finished. Game #{}, Turn #{}", saveGameTotal, game.getTurn());
        response.sendRedirect(Location.MENU_LOAD);
    }

    @SuppressWarnings("DataFlowIssue")
    private void endGame(HttpServletResponse response, Game game, HttpSession session)
            throws IOException {
        if (isGameStart(game == null, session, response)) return;

        log.info("Set game status to DEFEAT");
        game.setStatus(GameStatus.DEFEAT);
        response.sendRedirect(Location.ROOT);
    }

    private void newGame(HttpServletResponse response, Game oldGame, Connection connection, HttpSession session)
            throws IOException {

        log.info("Try to create New game");
        String sessionId = session.getId();
        Game game = new Game();
        App.putGame(sessionId, game);

        MapGenerator mapGenerator = new MapGenerator(Generator.MIN_LAND_EMPTY, Generator.CHANCE_REWARD,
                Generator.CHANCE_ENEMY, Generator.MAP_SIZE);
        game.setMapGenerator(mapGenerator);
        mapGenerator.generate(game.getCellList(), connection);

        if (oldGame != null && oldGame.isAdminAccess()) {
            log.info("Set admin access for New Game.");
            game.setAdminAccess(true);
        }

        log.info("New game is created.");
        game.setStatus(GameStatus.NOTHING);
        response.sendRedirect(Location.GAME);
    }

    private void loadGame(HttpServletRequest request, HttpServletResponse response, Connection connection,
                          HttpSession session) throws IOException {
        log.info("Load new game - start.");

        String gameNumber = request.getParameter(Attribute.ID);
        if (gameNumber.isEmpty()) {
            response.sendRedirect(Location.ROOT);
            return;
        }

        String sessionId = session.getId();
        String jsonGame;
        String query = "SELECT json FROM save WHERE number = ? AND session = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(gameNumber));
            statement.setString(2, sessionId);
            ResultSet rs = statement.executeQuery();
            if (!rs.first()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            jsonGame = rs.getString("json");
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (jsonGame.isEmpty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        Game loadGame = jsonToGame(jsonGame);
        App.putGame(sessionId, loadGame);

        log.info("Load new game - finish. Game #{}, Turn #{}.", gameNumber, loadGame.getTurn());
        session.setAttribute(Attribute.FLASH,
                "Game #" + gameNumber + " (Turn "+ loadGame.getTurn() +") successfully loaded.");
        session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_SUCCESS);
        response.sendRedirect(Location.GAME);
    }

    private void deleteGame(HttpServletRequest request, HttpServletResponse response, Connection connection,
                            HttpSession session) throws IOException {
        log.info("Delete game - start");
        String gameNumber = request.getParameter(Attribute.ID);
        if (gameNumber.isEmpty()) {
            response.sendRedirect(Location.ROOT);
            return;
        }

        String sessionId = session.getId();
        String query = "DELETE FROM save WHERE number = ? AND session = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(gameNumber));
            statement.setString(2, sessionId);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error access to SQL for {}", query);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        log.info("Delete game - finish. Game deleted #{}", gameNumber);
        session.setAttribute(Attribute.FLASH, "Game #" + gameNumber + " successfully deleted.");
        session.setAttribute(Attribute.FLASHTYPE, Attribute.ALERT_SUCCESS);
        response.sendRedirect(Location.MENU_LOAD);
    }
}
