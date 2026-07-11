<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head><title>Students</title></head>
<body>
  <h2>Students from Database</h2>
  <table border="1" cellpadding="8">
    <tr><th>ID</th><th>Name</th><th>Email</th></tr>
    <c:forEach var="s" items="${students}">
      <tr>
        <td>${s.id}</td>
        <td>${s.name}</td>
        <td>${s.email}</td>
      </tr>
    </c:forEach>
  </table>
</body>
</html>