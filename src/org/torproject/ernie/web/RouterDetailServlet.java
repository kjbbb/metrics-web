package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class RouterDetailServlet extends HttpServlet {

  private Connection conn = null;
  private String connectionURL;

  public void init() {

    /* Try to load the database driver. */
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      /* Don't initialize conn and always reply to all requests with
       * "500 internal server error". */
      return;
    }

    /* Read JDBC URL from deployment descriptor. */
    connectionURL = getServletContext().
        getInitParameter("jdbcUrl");

  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    String fingerprint;
    String validafter;

    try {
      fingerprint = request.getParameter("r");
      validafter = request.getParameter("va");
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    String query = "SELECT * FROM statusentry "
        + "WHERE fingerprint = ? "
        + "AND validafter = ? "
        + "LIMIT 1";

    try {
      conn = DriverManager.getConnection(connectionURL);
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, fingerprint);
      ps.setString(2, validafter);
      conn.close();
    } catch (SQLException e)  {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher("WEB-INF/routerdetail.jsp").forward(request,
        response);
  }
}
