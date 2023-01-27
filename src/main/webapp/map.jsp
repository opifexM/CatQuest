<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.javarush.maximov.settings.LandType" %>

<div class="container text-center">
<form>
    <c:forEach var="cell" items="${map}">
        <c:if test="${cell.isStartLine()}">
            <div class="row">
            <div class="col">
        </c:if>
        <c:choose>
            <c:when test="${currentcell.getId() eq cell.getId() && cell.getLandType() ne LandType.WALL}">
                <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-dark bg-info"/>
                <% session.removeAttribute("currentcell"); %>
            </c:when>
            <c:when test="${!cell.isAvailable() && !game.isAdminAccess()}">
                <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-secondary" disabled/>
            </c:when>
            <c:when test="${cell.getLandType() eq LandType.ENTRANCE}">
                <c:if test="${!cell.isExplore()}">
                    <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn btn-primary" />
                </c:if>
                <c:if test="${cell.isExplore()}">
                    <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-outline-primary" />
                </c:if>
            </c:when>
            <c:when test="${cell.getLandType() eq LandType.EXIT}">
                <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-primary" />
            </c:when>
            <c:when test="${cell.getLandType() eq LandType.EMPTY}">
                <c:choose>
                    <c:when test="${cell.checkEnemyInCell() && cell.checkRewardInCell()}">
                        <c:if test="${!cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-warning" />
                        </c:if>
                        <c:if test="${cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-outline-warning" />
                        </c:if>
                    </c:when>
                    <c:when test="${cell.checkEnemyInCell()}">
                        <c:if test="${!cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-danger" />
                        </c:if>
                        <c:if test="${cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-outline-danger" />
                        </c:if>
                    </c:when>
                    <c:when test="${cell.checkRewardInCell()}">
                        <c:if test="${!cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-success" />
                        </c:if>
                        <c:if test="${cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-outline-success" />
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${!cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-light" />
                        </c:if>
                        <c:if test="${cell.isExplore()}">
                            <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-outline-dark" />
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <input formaction="/game/move?id=${cell.getId()}" formmethod="post" type="submit" value="X" class="btn btn-secondary" />
            </c:otherwise>
        </c:choose>
        <c:if test="${cell.isEndLine()}">
            </div>
            </div>
        </c:if>
    </c:forEach>
</form>
</div>