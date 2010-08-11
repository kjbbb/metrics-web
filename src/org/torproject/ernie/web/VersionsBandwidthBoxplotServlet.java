package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
import org.apache.log4j.Logger;
import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.digest.DigestUtils;

public class VersionsBandwidthBoxplotServlet extends HttpServlet {

  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private SimpleDateFormat simpledf;
  private static final Logger log;

  static {
    ErnieProperties props = new ErnieProperties();
    log = Logger.getLogger(VersionsBandwidthBoxplotServlet.class);
  }

  public VersionsBandwidthBoxplotServlet()  {
    this.graphName = "versions-bandwidth-boxplot";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_bandwidth_versions_boxplot('%s', '%s', '%s', limit=%s)";
    this.simpledf = new SimpleDateFormat("yyyy-MM-dd");
    this.simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    String md5file, start = "", end = "", path, query, limit;

    try {

      start = request.getParameter("start");
      end = request.getParameter("end");
      limit = request.getParameter("limit");
      if (limit == null) { limit = "0"; }

      /* Validate input */
      try {
        simpledf.parse(start);
        simpledf.parse(end);
        Integer.parseInt(limit);
      } catch (Exception e)  {
        log.info("User entered invalid date or limit: " + start + " : " + end + ", " + e);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" +
          end + "-" + limit);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path, limit);
      gcontroller.generateGraph(query, path);
      gcontroller.writeOutput(path, request, response);

    } catch (NullPointerException e)  {
      log.warn(e.toString());
    } catch (IOException e) {
      log.warn(e.toString());
    }
  }
}
