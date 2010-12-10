package org.torproject.ernie.web;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.logging.*;
import java.text.*;

import javax.naming.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.*;

public class RouterDetailServlet extends HttpServlet {


  private DataSource ds;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(NetworkStatusServlet.class.toString());

    /* Look up data source. */
    try {
      Context cxt = new InitialContext();
      this.ds = (DataSource) cxt.lookup("java:comp/env/jdbc/tordir");
      this.logger.info("Successfully looked up data source.");
    } catch (NamingException e) {
      this.logger.log(Level.WARNING, "Could not look up data source", e);
    }
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    String fingerprint;
    java.sql.Timestamp validafter;

    try {
      fingerprint = request.getParameter("fingerprint");
      validafter = new java.sql.Timestamp(
          Long.parseLong(request.getParameter("validafter")));

    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    String query = "SELECT s.*, d.uptime, d.platform "
        + "FROM statusentry s "
        + "JOIN descriptor d "
        + "ON s.descriptor = d.descriptor "
        + "WHERE s.fingerprint = ? "
        + "AND s.validafter = ? "
        + "LIMIT 1";

    try {
      Connection conn = this.ds.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, fingerprint);
      ps.setTimestamp(2, validafter);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        request.setAttribute("validafter", rs.getTimestamp("validafter"));
        request.setAttribute("nickname", rs.getString("nickname"));
        request.setAttribute("fingerprint", rs.getString("fingerprint"));
        request.setAttribute("descriptor", rs.getString("descriptor"));
        request.setAttribute("published", rs.getTimestamp("published"));
        request.setAttribute("address", rs.getString("address"));
        request.setAttribute("orport", rs.getInt("orport"));
        request.setAttribute("dirport", rs.getInt("dirport"));
        request.setAttribute("isauthority", rs.getBoolean("isauthority"));
        request.setAttribute("isbadexit", rs.getBoolean("isbadexit"));
        request.setAttribute("isbaddirectory", rs.getBoolean("isbaddirectory"));
        request.setAttribute("isexit", rs.getBoolean("isexit"));
        request.setAttribute("isfast", rs.getBoolean("isfast"));
        request.setAttribute("isguard", rs.getBoolean("isguard"));
        request.setAttribute("ishsdir", rs.getBoolean("ishsdir"));
        request.setAttribute("isnamed", rs.getBoolean("isnamed"));
        request.setAttribute("isstable", rs.getBoolean("isstable"));
        request.setAttribute("isrunning", rs.getBoolean("isrunning"));
        request.setAttribute("isunnamed", rs.getBoolean("isunnamed"));
        request.setAttribute("isvalid", rs.getBoolean("isvalid"));
        request.setAttribute("isv2dir", rs.getBoolean("isv2dir"));
        request.setAttribute("isv3dir", rs.getBoolean("isv3dir"));
        request.setAttribute("version", rs.getString("version"));
        request.setAttribute("bandwidth", rs.getBigDecimal("bandwidth"));
        request.setAttribute("ports", rs.getString("ports"));
        request.setAttribute("uptime", rs.getBigDecimal("uptime"));
        request.setAttribute("platform", rs.getString("platform"));
      }
      conn.close();

    } catch (SQLException e)  {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      this.logger.log(Level.WARNING, "Error ", e);
      return;
    }

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher("WEB-INF/routerdetail.jsp").forward(request,
        response);
  }
}
