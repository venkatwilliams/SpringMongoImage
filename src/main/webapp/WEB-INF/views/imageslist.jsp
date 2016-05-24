<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<p>List of images stored in db</p>
	<table border="1px solid black">
		<tr>
			<th>FileName</th>
			<th>ContentType</th>
			<th>Path</th>
			<th>Image</th>
		</tr>
		<c:forEach items="${images}" var="image">
			<tr>
				<td><c:out value="${image.name}" /></td>
				<td><c:out value="${image.type}" /></td>
				<td><c:out value="${image.path}" /></td>
				<td><img src="${image.path}" height=100 width=100 /></td>
			</tr>
		</c:forEach>
	</table>


</body>
</html>