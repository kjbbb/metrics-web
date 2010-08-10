package org.torproject.ernie.web;

import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

public class NetworkSizeImageServlet extends HttpServlet {

  private static final Logger log;
  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private SimpleDateFormat simpledf;

  static {
    log = Logger.getLogger(NetworkSizeImageServlet.class);
  }

  public NetworkSizeImageServlet()  {
    this.graphName = "networksize";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_networksize_line('%s', '%s', '%s')";
    this.simpledf = new SimpleDateFormat("yyyy-MM-dd");
    this.simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    try {
      String md5file, start, end, path, query;

      start = request.getParameter("start");
      end = request.getParameter("end");

      /* Validate input */
      simpledf.parse(start);
      simpledf.parse(end);

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" + end);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path);

      File f = new File(path);
      if (!f.exists()) {
        gcontroller.generateGraph(query);
      }

      gcontroller.writeOutput(path, request, response);

    } catch (NullPointerException e) {
    } catch (ParseException e) {
    } catch (IOException e) {
    }
  }
}
