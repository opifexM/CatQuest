<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.javarush.maximov.settings.LandType" %>

<div class="container text-left">
    <ul>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-primary"/>
            - Start playing by selecting the entrance to the cave
        </li>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-info"/>
            - The current position inside the cave.
        </li>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-secondary" disabled/>
            - Some areas are not available yet.
        </li>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-danger"/>
            - Monsters are blocking the passage through the cave.
        </li>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-warning"/>
            - A magic item and an aggressive monster are here.
        </li>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-success"/>
            - Special items will be beneficial.
        </li>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-secondary"/>
            - Walls restrict movements in the cave.
        </li>
        <li>
            <input formmethod="post" type="submit" value="?" class="btn btn-light"/>
            - Voids allow you to move safely around the cave.
        </li>
    </ul>
</div>