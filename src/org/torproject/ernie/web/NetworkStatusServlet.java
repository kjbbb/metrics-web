package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class NetworkStatusServlet extends HttpServlet {

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

    Set<Map<String, Object>> status = new HashSet<Map<String, Object>>();

    try {
      conn = DriverManager.getConnection(connectionURL);

      Statement statement = conn.createStatement();

      String query = "SELECT s.*, "
          + "d.uptime AS uptime, d.platform AS platform "
          + "FROM statusentry s "
          + "JOIN descriptor d "
          + "ON d.descriptor=s.descriptor "
          + "WHERE s.validafter = "
              + "(SELECT MAX(validafter) FROM statusentry)";


      ResultSet rs = statement.executeQuery(query);

      while (rs.next()) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("validafter", rs.getTimestamp(1));
        row.put("nickname", rs.getString(2));
        row.put("fingerprint", rs.getString(3));
        row.put("descriptor", rs.getString(4));
        row.put("published", rs.getTimestamp(5));
        row.put("address", rs.getString(6));
        row.put("orport", rs.getInt(7));
        row.put("dirport", rs.getInt(8));
        row.put("isauthority", rs.getBoolean(9));
        row.put("isbadexit", rs.getBoolean(10));
        row.put("isbaddirectory", rs.getBoolean(11));
        row.put("isexit", rs.getBoolean(12));
        row.put("isfast", rs.getBoolean(13));
        row.put("isguard", rs.getBoolean(14));
        row.put("ishsdir", rs.getBoolean(15));
        row.put("isnamed", rs.getBoolean(16));
        row.put("isstable", rs.getBoolean(17));
        row.put("isrunning", rs.getBoolean(18));
        row.put("isunnamed", rs.getBoolean(19));
        row.put("isvalid", rs.getBoolean(20));
        row.put("isv2dir", rs.getBoolean(21));
        row.put("isv3dir", rs.getBoolean(22));
        row.put("version", rs.getString(23));
        row.put("bandwidth", rs.getBigDecimal(24));
        row.put("ports", rs.getString(25));
        row.put("rawdesc", rs.getBytes(26));
        row.put("uptime", rs.getBigDecimal(27));
        row.put("platform", rs.getString(27));
        status.add(row);

        conn.close();
      }
      request.setAttribute("status", status);
    } catch (SQLException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher("WEB-INF/networkstatus.jsp").forward(request,
        response);
  }
}
