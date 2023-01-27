package com.javarush.maximov;

import com.javarush.maximov.servlet.GameServlet;
import com.javarush.maximov.servlet.MenuServlet;
import com.javarush.maximov.world.Game;
import jakarta.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Map<String, Game> mapSessionGame = new HashMap<>();

    private static int getPort() {
        log.info("Select port");
        String port = System.getenv("PORT");
        if (port != null) {
            return Integer.parseInt(port);
        }
        return 8080;
    }

    public static void main(String[] args) throws LifecycleException, SQLException, IOException {
        Connection dbConnection = dbConnectInit();
        tomcatStart(dbConnection);
    }

    private static Connection dbConnectInit() throws SQLException, IOException {
        log.info("Connect to SQL");
        Connection dbConnection = DriverManager.getConnection("jdbc:h2:./catquest");
        log.info("Create SQL statement");
        try (Statement statement = dbConnection.createStatement()) {
            log.info("Setup SQL Database init");
            String initSql = getFileContent("init.sql");
            statement.execute(initSql);
        }
        return dbConnection;

    }

    private static void tomcatStart(Connection sqlConnection) throws LifecycleException {
        log.info("Tomcat start");
        Tomcat app = getApp(getPort(), sqlConnection);
        app.start();
        app.getServer().await();
    }

    @SuppressWarnings("SameParameterValue")
    private static String getFileContent(String fileName) throws IOException {
        Path pathToSolution = Paths.get(fileName).toAbsolutePath();
        return Files.readString(pathToSolution).trim();
    }

    public static Tomcat getApp(int port, Connection dbConnection) {
        log.info("Prepare Servlet - start");
        Tomcat app = new Tomcat();

        app.setPort(port);
        app.getConnector();
        app.setBaseDir(System.getProperty("java.io.tmpdir"));

        Context ctx = app.addWebapp("", new File("src/main/webapp").getAbsolutePath());
        ServletContext servletContext = ctx.getServletContext();
        servletContext.setAttribute("dbConnection", dbConnection);

        Tomcat.addServlet(ctx, MenuServlet.class.getSimpleName(), new MenuServlet());
        ctx.addServletMappingDecoded("", MenuServlet.class.getSimpleName());
        ctx.addServletMappingDecoded("/menu/*", MenuServlet.class.getSimpleName());

        Tomcat.addServlet(ctx, GameServlet.class.getSimpleName(), new GameServlet());
        ctx.addServletMappingDecoded("/game/*", GameServlet.class.getSimpleName());

        log.info("Prepare Servlet - finish");
        return app;
    }

    public static Game getGame(String sessionId) {
        return mapSessionGame.get(sessionId);
    }

    public static void putGame(String sessionId, Game game) {
        log.info("Update Map Game. SessionID: {}", sessionId);
        mapSessionGame.put(sessionId, game);
    }
}
