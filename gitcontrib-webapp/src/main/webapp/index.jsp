<%
  final String queryString = request.getQueryString();
  final String redirectURL = "org.gitcontrib.GitContribApp/GitContrib.html" + (queryString == null ? "" : "?" + queryString);
  response.sendRedirect(redirectURL);
%>
