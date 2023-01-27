<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.javarush.maximov.settings.GameStatus" %>

<tag:navmenu>
    <c:choose>
        <c:when test="${game.getStatus() eq GameStatus.PLAY}">
            <p>Turn: <b>${game.getTurn()}</b></p>
        </c:when>
        <c:otherwise>
            <p>Description of the colors used on the map:</p>
        </c:otherwise>
    </c:choose>

    <div class="container">
        <div class="row">
            <div class="col-sm">
                <c:choose>
                    <c:when test="${game.getStatus() eq GameStatus.PLAY}">
                        <c:import url="event.jsp"/>
                    </c:when>
                    <c:otherwise>
                        <c:import url="help.jsp"/>
                    </c:otherwise>
                </c:choose>
                <p></p>
            </div>
            <div class="col-sm">
                <c:import url="map.jsp"/>
                <p></p>
                <c:if test="${game.getStatus() eq GameStatus.PLAY && game.getTurn() < 0}">
                    <c:import url="indicator.jsp"/>
                    <p></p>
                </c:if>
                <c:if test="${sessionScope.gameflash != null}">
                    <div class="alert + ${sessionScope.flashtype}" role="alert">${sessionScope.gameflash}</div>
                </c:if>
            </div>
        </div>
    </div>
</tag:navmenu>

