<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.javarush.maximov.settings.GameStatus" %>

<tag:navmenu>
    <div class="container">
        <div class="row">
            <div class="col-sm">
                <c:choose>
                    <c:when test="${game == null || game.getStatus() eq GameStatus.NOTHING}">
                        <p>Welcome to the <b>Cat Quest</b> game.</p>
                        <p>You need to save the cat and get to the exit.</p>
                        <img src="<c:url value="/pic/main.jpg"/>" class="img-fluid" alt="">
                    </c:when>
                    <c:when test="${game.getStatus() eq GameStatus.PLAY}">
                        <p>Turn: <b>${game.getTurn()}</b> - The game has already started.</p>
                        <p>You need to save the cat and get to the exit.</p>
                        <c:import url="help.jsp"/>
                    </c:when>
                    <c:when test="${game.getStatus() eq GameStatus.VICTORY}">
                        <p>Congratulations on your <b>victory!</b></p>
                        <p>In the next game, the map will be completely different.</p>
                        <img src="<c:url value="/pic/basic_3.jpg"/>" class="img-fluid" alt="">
                    </c:when>
                    <c:when test="${game.getStatus() eq GameStatus.DEFEAT}">
                        <p>You <b>have lost</b> this battle.</p>
                        <p>Try again, the map is randomly generated each time.</p>
                        <img src="<c:url value="/pic/basic_5.jpg"/>" class="img-fluid" alt="">
                    </c:when>
                </c:choose>
                <p></p>
            </div>
            <div class="col-sm">
                <c:import url="menu.jsp"/>
                <p></p>
            </div>
        </div>
    </div>
</tag:navmenu>

