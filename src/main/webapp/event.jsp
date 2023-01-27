<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.javarush.maximov.settings.LandType" %>

<div class="container text-center">
<form>
    <c:choose>
        <c:when test="${sessionScope.retreat != null}">
            <% session.removeAttribute("retreat"); %>
            <img src="<c:url value="/pic/basic_4.jpg"/>" class="img-fluid rounded" width="500" height="600" alt="">
            <p></p>
            <p>You have decided to <b>retreat</b>.</p>
            <p>There is time to regroup and gain strength, or you can find a workaround.</p>
            <p></p>
        </c:when>
        <c:when test="${currentcell.getLandType() eq LandType.EXIT && currentcell.isExplore() == false}">
            <img src="<c:url value="/pic/basic_2.jpg"/>" class="img-fluid rounded" width="500" height="600" alt="">
            <p></p>
            <p><b>Exit</b> from the cave</p>
            <p>Congratulations, you're almost there.</p>
            If you exit the cave it will <b>end the game</b>.
            <p></p>
            <input formaction="/game/action?id=${currentcell.getId()}" formmethod="post" type="submit" value="Finish game!" class="btn btn-info" />
        </c:when>
        <c:when test="${(currentcell.getLandType() eq LandType.ENTRANCE && currentcell.isExplore() == false)
                        || (game.getTurn() == 0)}">
            <img src="<c:url value="/pic/basic_1.jpg"/>" class="img-fluid rounded" width="500" height="600" alt="">
            <p></p>
            <p><b>Entrance</b> to the cave</p>
            <p>There are many dangers ahead of you. Get ready for the challenges!</p>
            The <b>health</b> indicator will help you survive in battle.<br>
            The <b>mana</b> indicator will allow you to defeat opponents.<br>
            <p></p>
            <input formaction="/game/action?id=${currentcell.getId()}" formmethod="post" type="submit" value="Start exploring!" class="btn btn-info" />
        </c:when>

        <c:when test="${currentcell.isExplore() == true || currentcell.getId() == null}">
            <img src="<c:url value="/pic/basic_6.jpg"/>" class="img-fluid rounded" width="500" height="600"  alt="">
            <p></p>
            <p>There is <b>nothing</b> in this area.</p>
            <p>Try to look for cave areas that have not yet been discovered.</p>
            <p></p>
        </c:when>

        <c:when test="${currentcell.getLandType() eq LandType.WALL}">
            <img src="<c:url value="/pic/basic_7.jpg"/>" class="img-fluid rounded" width="500" height="600" alt="">
            <p></p>
            <p>You have approached the cave <b>wall</b>.</p>
            <p>You try to move the wall, but failed.</p>
            Try to find a workaround.
            <p></p>
        </c:when>
        <c:when test="${currentcell.checkEnemyInCell() && currentcell.checkRewardInCell()}">
            <img src="<c:url value="/pic/enemy_${enemyid}.jpg"/>" class="img-fluid rounded" width="500" height="600" alt="">
            <p></p>
            <p>You have met the enemy: <b>${enemyname}</b></p>
            <p>${enemycomment}</p>
            <p>Number of enemies: <b>${enemycount}</b></p>
            <p>A <b>special magical items</b> guarded by a monster are also seen!</p>
            <input formaction="/game/action?id=${currentcell.getId()}" formmethod="post" type="submit" value="Start a fight!" class="btn btn-danger" />
            <input formaction="/game/retreat" formmethod="post" type="submit" value="Retreat" class="btn btn-info" />
        </c:when>
        <c:when test="${currentcell.checkEnemyInCell()}">
            <img src="<c:url value="/pic/enemy_${enemyid}.jpg"/>" class="img-fluid rounded" width="500" height="500" alt="">
            <p></p>
            <p>You have met the enemy: <b>${enemyname}</b></p>
            <p>${enemycomment}</p>
            <p>Number of enemies: <b>${enemycount}</b></p>
            <input formaction="/game/action?id=${currentcell.getId()}" formmethod="post" type="submit" value="Start a fight!" class="btn btn-danger" />
            <input formaction="/game/retreat" formmethod="post" type="submit" value="Retreat" class="btn btn-info" />
        </c:when>
        <c:when test="${currentcell.checkRewardInCell()}">
            <img src="<c:url value="/pic/reward_${rewardid}.jpg"/>" class="img-fluid rounded" width="500" height="600" alt="">
            <p></p>
            <p>You have found a magic item: <b>${rewardname}</b></p>
            <p>${rewardcomment}</p>
            <p>Number of items: <b>${rewardcount}</b></p>
            <input formaction="/game/action?id=${currentcell.getId()}" formmethod="post" type="submit" value="Take everything" class="btn btn-success" />
            <input formaction="/game/retreat" formmethod="post" type="submit" value="Retreat" class="btn btn-info" />
        </c:when>
        <c:otherwise>
            <img src="<c:url value="/pic/basic_6.jpg"/>" class="img-fluid rounded" width="500" height="600" alt="">
            <p></p>
            <p>There is <b>nothing</b> in this area.</p>
            <p>But by exploring the area you will expand the map.</p>
            <p></p>
            <input formaction="/game/action?id=${currentcell.getId()}" formmethod="post" type="submit" value="Exploring" class="btn btn-info" />
        </c:otherwise>
    </c:choose>
</form>
</div>