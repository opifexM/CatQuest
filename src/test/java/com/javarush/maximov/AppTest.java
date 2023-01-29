package com.javarush.maximov;

import com.javarush.maximov.settings.LandType;
import com.javarush.maximov.world.Cell;
import com.javarush.maximov.world.Game;
import kong.unirest.Cookie;
import kong.unirest.Cookies;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.javarush.maximov.App.getApp;
import static org.assertj.core.api.Assertions.assertThat;


class AppTest {

    private static String baseUrl;
    private static Tomcat app;
    private static String sessionId;

    @BeforeAll
    public static void beforeAll() throws SQLException, IOException, LifecycleException {
        Connection dbConnection = App.dbConnectInit();
        int port = App.getPort();
        app = getApp(port, dbConnection);
        app.start();
        baseUrl = "http://localhost:" + port;

        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        Cookies cookies = response.getCookies();
        Optional<Cookie> optionalCookie = cookies.stream()
                .filter(cookie1 -> cookie1.getName().equals("JSESSIONID")).findFirst();
        optionalCookie.ifPresent(cookie -> sessionId = cookie.getValue());
    }

    @AfterAll

    public static void afterAll() throws LifecycleException {
        app.stop();
    }

    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testMenuServlet_ShowMainMenu() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Main Menu");
        assertThat(content).contains("Session ID");
    }

    @Test
    void testMenuServlet_StartNewGame() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/game")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Description of the colors used on the map");
        assertThat(content).contains("blue square");
    }

    @Test
    @Order(1)
    void testGameServlet_FirstTurn() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        Game game = App.getGame(sessionId);
        Optional<Cell> optionalCell = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
        int id = optionalCell.orElseThrow().getId();

        responsePost = Unirest
                .post(baseUrl + "/game/move?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/game")
                .asString();
        String content = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Start exploring");


        responsePost = Unirest
                .post(baseUrl + "/game/action?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");

        response = Unirest
                .get(baseUrl + "/game")
                .asString();
        content = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Try to look for cave areas that have not yet been discovered");
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"Second round", "Third round", "Fourth round", "Fifth round"})
    void testGameServlet_NextTurn() {
        Game game = App.getGame(sessionId);
        int turn = game.getTurn();
        List<Cell> cellAvailableList = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() != LandType.WALL)
                .filter(Cell::isAvailable)
                .toList();

        for (Cell cell : cellAvailableList) {
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/game/action?id=" + cell.getId())
                    .asString();
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");
            turn++;
        }

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/game")
                .asString();
        String content = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("<b>" + (turn) + "</b>");
    }

    @Test
    void testMenuServlet_SaveGame() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        Game game = App.getGame(sessionId);
        Optional<Cell> optionalCell = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
        int id = optionalCell.orElseThrow().getId();

        responsePost = Unirest
                .post(baseUrl + "/game/move?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/game/action?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/menu/save")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/menu/load");


        HttpResponse<String> response = Unirest
                .get(baseUrl + "/menu/load")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Load Game");
        assertThat(content).contains("has been saved successfully");
    }

    @Test
    void testMenuServlet_LoadGame() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        Game game = App.getGame(sessionId);
        Optional<Cell> optionalCell = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
        int id = optionalCell.orElseThrow().getId();

        responsePost = Unirest
                .post(baseUrl + "/game/move?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/game/action?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/menu/save")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/menu/load");


        responsePost = Unirest
                .post(baseUrl + "/menu/load?id=1")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        HttpResponse<String> response = Unirest
                .get(baseUrl + "/game")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("successfully loaded");
    }

    @Test
    void testMenuServlet_DeleteGame() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        Game game = App.getGame(sessionId);
        Optional<Cell> optionalCell = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
        int id = optionalCell.orElseThrow().getId();

        responsePost = Unirest
                .post(baseUrl + "/game/move?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/game/action?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/menu/save")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/menu/load");

        responsePost = Unirest
                .post(baseUrl + "/menu/save")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/menu/load");

        responsePost = Unirest
                .post(baseUrl + "/menu/delete?id=1")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/menu/load");


        HttpResponse<String> response = Unirest
                .get(baseUrl + "/menu/load")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Game #1 successfully deleted.");
    }


    @Test
    void testMenuServlet_EndGame() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        Game game = App.getGame(sessionId);
        Optional<Cell> optionalCell = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
        int id = optionalCell.orElseThrow().getId();

        responsePost = Unirest
                .post(baseUrl + "/game/move?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/game/action?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/menu/end")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");


        HttpResponse<String> response = Unirest
                .get(baseUrl + "/")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Try again, the map is randomly generated each time");
    }

    @Test
    void testMenuServlet_AdminAccessOn() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        Game game = App.getGame(sessionId);
        Optional<Cell> optionalCell = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
        int id = optionalCell.orElseThrow().getId();

        responsePost = Unirest
                .post(baseUrl + "/game/move?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/game/action?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/menu/admin")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/game")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Admin access enabled");
    }

    @Test
    void testMenuServlet_AdminAccessOff() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/menu/new")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        Game game = App.getGame(sessionId);
        Optional<Cell> optionalCell = game.getCellList().values().stream()
                .filter(cell -> cell.getLandType() == LandType.ENTRANCE).findFirst();
        int id = optionalCell.orElseThrow().getId();

        responsePost = Unirest
                .post(baseUrl + "/game/move?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/game/action?id=" + id)
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");


        responsePost = Unirest
                .post(baseUrl + "/menu/admin")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");

        responsePost = Unirest
                .post(baseUrl + "/menu/admin")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/game");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/game")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Admin access disabled");
    }
}