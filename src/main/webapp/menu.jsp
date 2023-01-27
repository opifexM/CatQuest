<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.javarush.maximov.settings.GameStatus" %>

<div class="container text-center">
    <form>
        <p></p>
        <h3>Main Menu</h3>
        <c:choose>
            <c:when test="${game.getStatus() eq GameStatus.PLAY}">
                <input formaction="/game" formmethod="get" type="submit" value="Continue game" class="btn btn-success" />
                <p></p>
                <input formaction="/menu/load" formmethod="get" type="submit" value="Load game" class="btn btn-info" />
                <p></p>
                <input formaction="/menu/save" formmethod="post" type="submit" value="Save game" class="btn btn-info" />
                <p></p>
                <input formaction="/menu/end" formmethod="post" type="submit" value="End game" class="btn btn-danger" />
                <p></p>
            </c:when>
            <c:otherwise>
                <input formaction="/menu/new" formmethod="post" type="submit" value="New game" class="btn btn-success" />
                <p></p>
                <input formaction="/menu/load" formmethod="get" type="submit" value="Load game" class="btn btn-info" />
                <p></p>
                <input formaction="/menu/save" formmethod="post" type="submit" value="Save game" class="btn btn-info" disabled />
                <p></p>
                <input formaction="/menu/end" formmethod="post" type="submit" value="End game" class="btn btn-danger" disabled />
                <p></p>
            </c:otherwise>
        </c:choose>
        Session ID: ${sessionid}
    </form>
</div>