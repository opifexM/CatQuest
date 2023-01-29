<%@ tag description="Layout" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-GLhlTQ8iRABdZLl6O3oVMWSktQOp6b7In1Zl3/Jr59b6EGGoI1aFkw7cmDA6j6gD" crossorigin="anonymous">
    <title>Cat Quest</title>
</head>

<body class="d-flex flex-column min-vh-100">
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="<c:url value="/"/>">Cat Quest</a>
        <button class="navbar-toggler collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="navbar-collapse collapse" id="navbarNav" style="">
            <div class="navbar-nav">
                <a class="nav-link" href="<c:url value="/"/>">Main Menu</a>
                <a class="nav-link" href="<c:url value="/game"/>">Game</a>
                <c:choose>
                    <c:when test="${game.isAdminAccess() == true}">
                        <form>
                            <input formaction="/menu/admin" formmethod="post" type="submit" value="Admin ON" class="btn btn-danger btn-sm" />
                            <input formaction="/menu/new" formmethod="post" type="submit" value="Generate map" class="btn btn-success btn-sm" />
                        </form>
                    </c:when>
                    <c:when test="${game.isAdminAccess() == false}">
                        <form>
                            <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#staticBackdrop">Admin</button>
                        </form>
                    </c:when>
                </c:choose>
            </div>
        </div>
    </div>
</nav>

<div class="container mt-3">
    <div class="card">
        <div class="card-body">
            <jsp:doBody/>
        </div>
    </div>
    <c:if test="${sessionScope.flash != null}">
        <div class="alert + ${sessionScope.flashtype}" role="alert">${sessionScope.flash}</div>
    </c:if>
    <% session.removeAttribute("flash"); %>
    <% session.removeAttribute("gameflash"); %>
</div>

<div class="modal fade" id="staticBackdrop" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5" id="staticBackdropLabel">Admin Access</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                Do you want to enable administrator access?
                <br>Admin access will give you an unfair advantage in the game.
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <form>
                    <input formaction="/menu/admin" formmethod="post" type="submit" value="Activate access" class="btn btn-danger" />
                </form>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js" integrity="sha384-w76AqPfDkMBDXo30jS1Sgez6pr3x5MlQ1ZAGC+nuZB+EYdgRZgiwxhTBTkF7CXvN" crossorigin="anonymous"></script>
</body>
</html>
