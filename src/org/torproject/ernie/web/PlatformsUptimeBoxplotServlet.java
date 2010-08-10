package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
import org.apache.log4j.Logger;
import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.digest.DigestUtils;

public class PlatformsUptimeBoxplotServlet extends HttpServlet {

  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private SimpleDateFormat simpledf;
  private static final Logger log;

  static {
    ErnieProperties props = new ErnieProperties();
    log = Logger.getLogger(PlatformsUptimeBoxplotServlet.class);
  }

  public PlatformsUptimeBoxplotServlet()  {
    this.graphName = "platform-uptime-boxplot";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_platform_uptime_boxlpot('%s', '%s', '%s', limit=%s)";
    this.simpledf = new SimpleDateFormat("yyyy-MM-dd");
    this.simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    String md5file, start = "", end = "", path, query, limit;
    log.warn("sdcsdcs");

    try {

      start = request.getParameter("start");
      end = request.getParameter("end");
      limit = (request.getParameter("limit") == null) ? "0" :
          request.getParameter("limit") ;

      /* Validate input */
      try {
        simpledf.parse(start);
        simpledf.parse(end);
        Integer.parseInt(limit);
      } catch (ParseException e)  {
        start = (start == null) ? "null" : start;
        end = (end == null) ? "null" : end;
        log.info("User entered invalid date: " + start + " : " + end + ", " + e);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      }

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" +
          end + "-" + limit);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path, limit);

      File f = new File(path);
      if (!f.exists()) {
        gcontroller.generateGraph(query);
      }

      gcontroller.writeOutput(path, request, response);

    } catch (NullPointerException e)  {
      log.warn(e.toString());
    } catch (IOException e) {
      log.warn(e.toString());
    }
  }
}
