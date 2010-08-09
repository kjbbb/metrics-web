package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
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
  private final Constants c;

  static {
    ErnieProperties props = new ErnieProperties();
  }

  public PlatformsUptimeBoxplotServlet()  {
    this.graphName = "platform-uptime-boxplot";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_platform_uptime_boxlpot('%s', '%s', '%s', limit=%s)";
    this.c = new Constants();
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    try {
      String md5file, start, end, path, query, limit;

      start = request.getParameter("start");
      end = request.getParameter("end");
      limit = (request.getParameter("limit") == null) ? "0" :
          request.getParameter("limit") ;

      /* Validate input */
      c.simpledf.parse(start);
      c.simpledf.parse(end);

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" +
          end + "-" + limit);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path, limit);

      File f = new File(path);
      if (!f.exists()) {
        gcontroller.generateGraph(query);
      }

      gcontroller.writeOutput(path, request, response);

    } catch (ParseException e)  {
    } catch (NullPointerException e)  {
    } catch (IOException e) {
    }
  }
}
