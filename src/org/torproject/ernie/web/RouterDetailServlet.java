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
      fingerprint = request.getParameter("fingerprint");
      validafter = request.getParameter("validafter");
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    String query = "SELECT s.*, d.uptime, d.platform "
        + "FROM statusentry s "
        + "JOIN descriptor d "
        + "ON s.descriptor = d.descriptor "
        + "WHERE s.fingerprintt = ? "
        + "AND s.validafter = ? "
        + "LIMIT 1";

    try {
      conn = DriverManager.getConnection(connectionURL);
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, fingerprint);
      ps.setString(2, validafter);
      ResultSet rs = ps.executeQuery();
      if (rs.first()) {
        request.setAttribute("validafter", rs.getTimestamp(1));
        request.setAttribute("nickname", rs.getString(2));
        request.setAttribute("fingerprint", rs.getString(3));
        request.setAttribute("descriptor", rs.getString(4));
        request.setAttribute("published", rs.getTimestamp(5));
        request.setAttribute("address", rs.getString(6));
        request.setAttribute("orport", rs.getInt(7));
        request.setAttribute("dirport", rs.getInt(8));
        request.setAttribute("isauthority", rs.getBoolean(9));
        request.setAttribute("isbadexit", rs.getBoolean(10));
        request.setAttribute("isbaddirectory", rs.getBoolean(11));
        request.setAttribute("isexit", rs.getBoolean(12));
        request.setAttribute("isfast", rs.getBoolean(13));
        request.setAttribute("isguard", rs.getBoolean(14));
        request.setAttribute("ishsdir", rs.getBoolean(15));
        request.setAttribute("isnamed", rs.getBoolean(16));
        request.setAttribute("isstable", rs.getBoolean(17));
        request.setAttribute("isrunning", rs.getBoolean(18));
        request.setAttribute("isunnamed", rs.getBoolean(19));
        request.setAttribute("isvalid", rs.getBoolean(20));
        request.setAttribute("isv2dir", rs.getBoolean(21));
        request.setAttribute("isv3dir", rs.getBoolean(22));
        request.setAttribute("version", rs.getString(23));
        request.setAttribute("bandwidth", rs.getBigDecimal(24));
        request.setAttribute("ports", rs.getString(25));
        request.setAttribute("uptime", rs.getBigDecimal(26));
        request.setAttribute("platform", rs.getString(27));
      } else {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
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
