<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<tag:navmenu>
<div class="container text-center">
    <form>
        <p></p>
        <h3>Load Game (${savetotal}/10)</h3>
        <p>Session ID: ${sessionid}</p>
        <table class="table">
            <thead>
            <tr>
                <th>Game #</th>
                <th>Turn</th>
                <th>Date</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="save" items="${saves}">
            <tr>
                <td>Game #${save.number()}</td>
                <td>${save.turn()}</td>
                <td>${save.date()}</td>
                <td><input formaction="/menu/load?id=${save.number()}" formmethod="post" type="submit" value="Load" class="btn btn-success" /></td>
                <td><input formaction="/menu/delete?id=${save.number()}" formmethod="post" type="submit" value="Delete" class="btn btn-danger" /></td>
            </tr>
            </c:forEach>
            </tbody>
        </table>
    </form>
</div>
</tag:navmenu>